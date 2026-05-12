package com.board.server.upload;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/uploads")
public class ImageUploadController {

	private final ImageUploadService imageUploadService;

	public ImageUploadController(ImageUploadService imageUploadService) {
		this.imageUploadService = imageUploadService;
	}

	@PostMapping("/images")
	public ImageUploadResponse uploadImage(@RequestPart MultipartFile file) {
		return imageUploadService.upload(file);
	}

	@GetMapping("/images/{fileId}")
	public ResponseEntity<byte[]> downloadImage(@PathVariable Long fileId) {
		UploadedImage image = imageUploadService.download(fileId);
		String contentType = Objects.requireNonNullElse(image.contentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE);

		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_LENGTH, String.valueOf(image.contentLength()))
			.contentType(MediaType.parseMediaType(contentType))
			.cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
			.body(image.content());
	}
}
