package com.board.server.comment;

import java.time.LocalDateTime;
import java.util.List;

public record CommentResponse(
	Long id,
	Long postId,
	Long parentId,
	String author,
	String content,
	boolean deleted,
	boolean edited,
	LocalDateTime createdAt,
	LocalDateTime updatedAt,
	List<CommentResponse> replies
) {
}
