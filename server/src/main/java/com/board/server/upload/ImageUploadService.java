package com.board.server.upload;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.board.server.config.S3Properties;
import com.board.server.postfile.PostFile;
import com.board.server.postfile.PostFileMapper;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class ImageUploadService {

	private static final Logger log = LoggerFactory.getLogger(ImageUploadService.class);

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
		"image/jpeg",
		"image/png",
		"image/gif",
		"image/webp"
	);

	private final S3Client s3Client;
	private final S3Properties s3Properties;
	private final PostFileMapper postFileMapper;

	public ImageUploadService(S3Client s3Client, S3Properties s3Properties, PostFileMapper postFileMapper) {
		this.s3Client = s3Client;
		this.s3Properties = s3Properties;
		this.postFileMapper = postFileMapper;
	}

	public ImageUploadResponse upload(MultipartFile file) {
		validateImage(file);

		String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
		String extension = getExtension(originalName);
		String storedName = UUID.randomUUID() + extension;
		String key = "images/" + storedName;

		try {
			PutObjectRequest request = PutObjectRequest.builder()
				.bucket(s3Properties.bucket())
				.key(key)
				.contentType(file.getContentType())
				.contentLength(file.getSize())
				.build();

			s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

			PostFile postFile = new PostFile();
			postFile.setOriginalName(originalName);
			postFile.setStoredName(storedName);
			postFile.setFilePath(key);
			postFile.setContentType(file.getContentType());
			postFile.setFileSize(file.getSize());
			postFileMapper.insert(postFile);

			return new ImageUploadResponse(
				postFile.getId(),
				"/api/uploads/images/" + postFile.getId(),
				originalName,
				file.getSize()
			);
		} catch (IOException | SdkException exception) {
			log.error(
				"Image upload failed. bucket={}, key={}, originalName={}, contentType={}, size={}",
				s3Properties.bucket(),
				key,
				originalName,
				file.getContentType(),
				file.getSize(),
				exception
			);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Image upload failed.", exception);
		}
	}

	public UploadedImage download(Long fileId) {
		PostFile postFile = postFileMapper.findById(fileId)
			.filter(this::isImageFile)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found."));

		try {
			GetObjectRequest request = GetObjectRequest.builder()
				.bucket(s3Properties.bucket())
				.key(postFile.getFilePath())
				.build();
			ResponseBytes<GetObjectResponse> object = s3Client.getObjectAsBytes(request);

			return new UploadedImage(
				object.asByteArray(),
				object.response().contentType(),
				object.response().contentLength()
			);
		} catch (NoSuchKeyException exception) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found.");
		} catch (SdkException exception) {
			log.error(
				"Image download failed. bucket={}, key={}, fileId={}",
				s3Properties.bucket(),
				postFile.getFilePath(),
				fileId,
				exception
			);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Image download failed.", exception);
		}
	}

	public void deleteUnattached(Long fileId) {
		PostFile postFile = postFileMapper.findById(fileId)
			.filter(this::isImageFile)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found."));

		if (postFile.getPostId() != null || postFile.getCommentId() != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Attached images are deleted when the content is updated.");
		}

		deleteImage(postFile);
	}

	public void deleteImages(Iterable<PostFile> images) {
		for (PostFile image : images) {
			if (isImageFile(image)) {
				deleteImage(image);
			}
		}
	}

	private void validateImage(MultipartFile file) {
		if (file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select an image file.");
		}

		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files can be uploaded.");
		}
	}

	private boolean isImageFile(PostFile file) {
		return file.getFilePath() != null && file.getFilePath().startsWith("images/");
	}

	private void deleteImage(PostFile file) {
		try {
			DeleteObjectRequest request = DeleteObjectRequest.builder()
				.bucket(s3Properties.bucket())
				.key(file.getFilePath())
				.build();
			s3Client.deleteObject(request);
			postFileMapper.deleteById(file.getId());
		} catch (SdkException exception) {
			log.error(
				"Image delete failed. bucket={}, key={}, fileId={}",
				s3Properties.bucket(),
				file.getFilePath(),
				file.getId(),
				exception
			);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Image delete failed.", exception);
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
