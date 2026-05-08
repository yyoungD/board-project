package com.board.server.upload;

public record UploadedImage(
	byte[] content,
	String contentType,
	Long contentLength
) {
}
