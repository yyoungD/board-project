package com.board.server.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
	Long parentId,

	@NotBlank
	@Size(max = 2000)
	String content
) {
}
