package com.board.server.upload;

public record ImageUploadResponse(
	String url,
	String originalName,
	long size
) {
}
