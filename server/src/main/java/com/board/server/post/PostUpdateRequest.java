package com.board.server.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
	@NotBlank
	@Size(max = 200)
	String title,

	@NotBlank
	String content
) {
}
