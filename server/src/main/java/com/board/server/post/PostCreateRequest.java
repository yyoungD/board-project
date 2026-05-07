package com.board.server.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostCreateRequest(
	@NotBlank
	@Size(max = 200)
	String title,

	@NotBlank
	@Size(max = 100)
	String author,

	@NotBlank
	String content
) {
}
