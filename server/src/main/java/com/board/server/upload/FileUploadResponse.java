package com.board.server.upload;

public record FileUploadResponse(
	Long id,
	String url,
	String originalName,
	long size
) {
}
