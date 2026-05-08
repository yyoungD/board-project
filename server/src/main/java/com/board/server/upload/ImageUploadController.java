package com.board.server.upload;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
}
