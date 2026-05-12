package com.board.server.upload;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

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
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class ImageUploadService {

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
				"/api/uploads/images/" + postFile.getId(),
				originalName,
				file.getSize()
			);
		} catch (IOException | SdkException exception) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다.");
		}
	}

	public UploadedImage download(Long fileId) {
		PostFile postFile = postFileMapper.findById(fileId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."));

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
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다.");
		} catch (SdkException exception) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지를 불러오지 못했습니다.");
		}
	}

	private void validateImage(MultipartFile file) {
		if (file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일을 선택해 주세요.");
		}

		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드할 수 있습니다.");
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
