package com.board.server.comment;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CommentController {

	private final CommentService commentService;

	public CommentController(CommentService commentService) {
		this.commentService = commentService;
	}

	@GetMapping("/posts/{postId}/comments")
	public List<CommentResponse> findByPostId(@PathVariable Long postId) {
		return commentService.findByPostId(postId);
	}

	@PostMapping("/posts/{postId}/comments")
	public ResponseEntity<CommentResponse> create(
		@PathVariable Long postId,
		@Valid @RequestBody CommentCreateRequest request,
		@AuthenticationPrincipal Jwt jwt
	) {
		CommentResponse createdComment = commentService.create(postId, request, jwt.getSubject());
		return ResponseEntity
			.created(URI.create("/api/comments/" + createdComment.id()))
			.body(createdComment);
	}

	@PutMapping("/comments/{id}")
	public CommentResponse update(
		@PathVariable Long id,
		@Valid @RequestBody CommentUpdateRequest request,
		@AuthenticationPrincipal Jwt jwt
	) {
		return commentService.update(id, request, jwt.getSubject());
	}

	@DeleteMapping("/comments/{id}")
	public CommentResponse delete(
		@PathVariable Long id,
		@AuthenticationPrincipal Jwt jwt
	) {
		return commentService.delete(id, jwt.getSubject());
	}
}
