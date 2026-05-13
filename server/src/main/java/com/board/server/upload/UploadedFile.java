package com.board.server.upload;

public record UploadedFile(
	byte[] content,
	String contentType,
	long contentLength,
	String originalName
) {
}
