package com.board.server.post;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
	public List<Post> findAll() {
		return postService.findAll();
	}

	@GetMapping("/{id}")
	public Post findById(@PathVariable Long id) {
		return postService.findById(id);
	}

	@PostMapping
	public ResponseEntity<Post> create(@Valid @RequestBody PostCreateRequest request) {
		Post createdPost = postService.create(request);
		return ResponseEntity
			.created(URI.create("/api/posts/" + createdPost.getId()))
			.body(createdPost);
	}

	@PutMapping("/{id}")
	public Post update(@PathVariable Long id, @Valid @RequestBody PostUpdateRequest request) {
		return postService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteById(@PathVariable Long id) {
		postService.deleteById(id);
		return ResponseEntity.noContent().build();
	}
}
