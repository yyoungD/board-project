package com.board.server.post;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/posts")
public class PostController {

	private final PostService postService;

	public PostController(PostService postService) {
		this.postService = postService;
	}

	@GetMapping
	public PageResponse<Post> findPage(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		return postService.findPage(page, size);
	}

	@GetMapping("/{id}")
	public Post findById(@PathVariable Long id) {
		return postService.findById(id);
	}

	@PostMapping
	public ResponseEntity<Post> create(
		@Valid @RequestBody PostCreateRequest request,
		@AuthenticationPrincipal Jwt jwt
	) {
		Post createdPost = postService.create(request, jwt.getSubject());
		return ResponseEntity
			.created(URI.create("/api/posts/" + createdPost.getId()))
			.body(createdPost);
	}

	@PutMapping("/{id}")
	public Post update(
		@PathVariable Long id,
		@Valid @RequestBody PostUpdateRequest request,
		@AuthenticationPrincipal Jwt jwt
	) {
		return postService.update(id, request, jwt.getSubject());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(
		@PathVariable Long id,
		@AuthenticationPrincipal Jwt jwt
	) {
		postService.deleteById(id, jwt.getSubject());
		return ResponseEntity.noContent().build();
	}
}
