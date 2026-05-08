package com.board.server.upload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ImageUploadService {

	private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
		"image/jpeg",
		"image/png",
		"image/gif",
		"image/webp"
	);

	private final Path uploadDirectory = Path.of("uploads", "images").toAbsolutePath().normalize();

	public ImageUploadResponse upload(MultipartFile file) {
		if (file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일을 선택해 주세요.");
		}

		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드할 수 있습니다.");
		}

		try {
			Files.createDirectories(uploadDirectory);
			String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
			String extension = getExtension(originalName);
			String storedName = UUID.randomUUID() + extension;
			Path target = uploadDirectory.resolve(storedName).normalize();

			file.transferTo(target);

			return new ImageUploadResponse(
				"/uploads/images/" + storedName,
				originalName,
				file.getSize()
			);
		} catch (IOException exception) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다.");
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
