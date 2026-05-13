package com.board.server.upload;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;

import com.board.server.config.S3Properties;
import com.board.server.postfile.PostFile;
import com.board.server.postfile.PostFileMapper;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
public class FileUploadService {

	private static final Logger log = LoggerFactory.getLogger(FileUploadService.class);

	private final S3Client s3Client;
	private final S3Properties s3Properties;
	private final PostFileMapper postFileMapper;

	public FileUploadService(S3Client s3Client, S3Properties s3Properties, PostFileMapper postFileMapper) {
		this.s3Client = s3Client;
		this.s3Properties = s3Properties;
		this.postFileMapper = postFileMapper;
	}

	public FileUploadResponse upload(MultipartFile file) {
		validateFile(file);

		String originalName = file.getOriginalFilename() == null ? "attachment" : file.getOriginalFilename();
		String extension = getExtension(originalName);
		String storedName = UUID.randomUUID() + extension;
		String key = "files/" + storedName;

		try {
			String contentType = file.getContentType() == null
				? MediaType.APPLICATION_OCTET_STREAM_VALUE
				: file.getContentType();
			PutObjectRequest request = PutObjectRequest.builder()
				.bucket(s3Properties.bucket())
				.key(key)
				.contentType(contentType)
				.contentLength(file.getSize())
				.build();

			s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

			PostFile postFile = new PostFile();
			postFile.setOriginalName(originalName);
			postFile.setStoredName(storedName);
			postFile.setFilePath(key);
			postFile.setContentType(contentType);
			postFile.setFileSize(file.getSize());
			postFileMapper.insert(postFile);

			return new FileUploadResponse(
				postFile.getId(),
				"/api/uploads/files/" + postFile.getId(),
				originalName,
				file.getSize()
			);
		} catch (IOException | SdkException exception) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed.");
		}
	}

	public UploadedFile download(Long fileId) {
		PostFile postFile = postFileMapper.findById(fileId)
			.filter(this::isAttachmentFile)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found."));

		try {
			GetObjectRequest request = GetObjectRequest.builder()
				.bucket(s3Properties.bucket())
				.key(postFile.getFilePath())
				.build();
			ResponseBytes<GetObjectResponse> object = s3Client.getObjectAsBytes(request);

			return new UploadedFile(
				object.asByteArray(),
				object.response().contentType(),
				object.response().contentLength(),
				postFile.getOriginalName()
			);
		} catch (NoSuchKeyException exception) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found.");
		} catch (SdkException exception) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File download failed.");
		}
	}

	public void deleteUnattached(Long fileId) {
		PostFile postFile = postFileMapper.findById(fileId)
			.filter(this::isAttachmentFile)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found."));

		if (postFile.getPostId() != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Attached files are deleted when the post is updated.");
		}

		deleteFile(postFile);
	}

	public int cleanupUnattachedFiles(Duration maxAge) {
		LocalDateTime createdBefore = LocalDateTime.now().minus(maxAge);
		List<PostFile> files = postFileMapper.findUnattachedFilesCreatedBefore(createdBefore);
		int deletedCount = 0;

		for (PostFile file : files) {
			try {
				deleteFile(file);
				deletedCount++;
			} catch (ResponseStatusException exception) {
				log.warn("Failed to cleanup unattached file. fileId={}, path={}", file.getId(), file.getFilePath(), exception);
			}
		}

		return deletedCount;
	}

	public void deleteFiles(List<PostFile> files) {
		for (PostFile file : files) {
			if (isAttachmentFile(file)) {
				deleteFile(file);
			}
		}
	}

	public String contentDisposition(String originalName) {
		String encodedName = UriUtils.encode(originalName, StandardCharsets.UTF_8);
		return "attachment; filename=\"attachment\"; filename*=UTF-8''" + encodedName;
	}

	private void validateFile(MultipartFile file) {
		if (file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select a file to upload.");
		}
	}

	private boolean isAttachmentFile(PostFile file) {
		return file.getFilePath() != null && file.getFilePath().startsWith("files/");
	}

	private void deleteFile(PostFile file) {
		try {
			DeleteObjectRequest request = DeleteObjectRequest.builder()
				.bucket(s3Properties.bucket())
				.key(file.getFilePath())
				.build();
			s3Client.deleteObject(request);
			postFileMapper.deleteById(file.getId());
		} catch (SdkException exception) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File delete failed.", exception);
		}
	}

	private String getExtension(String filename) {
		int dotIndex = filename.lastIndexOf('.');
		if (dotIndex < 0) {
			return "";
		}
		return filename.substring(dotIndex).toLowerCase();
	}
}
