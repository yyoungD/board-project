package com.board.server.post;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.CookieValue;
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

	private static final String VIEWER_COOKIE_NAME = "board_viewer_id";

	private final PostService postService;

	public PostController(PostService postService) {
		this.postService = postService;
	}

	@GetMapping
	public PageResponse<Post> findPage(
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") int size,
		@RequestParam(defaultValue = "") String keyword
	) {
		return postService.findPage(page, size, keyword);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Post> findById(
		@PathVariable Long id,
		@AuthenticationPrincipal Jwt jwt,
		@CookieValue(name = VIEWER_COOKIE_NAME, required = false) String viewerCookie,
		HttpServletRequest request
	) {
		String nextViewerCookie = viewerCookie == null || viewerCookie.isBlank()
			? UUID.randomUUID().toString()
			: viewerCookie;
		String viewerKey = createViewerKey(jwt, nextViewerCookie, request);
		Post post = postService.findByIdAndCountView(id, viewerKey);

		if (viewerCookie == null || viewerCookie.isBlank()) {
			ResponseCookie cookie = ResponseCookie.from(VIEWER_COOKIE_NAME, nextViewerCookie)
				.httpOnly(true)
				.sameSite("Lax")
				.path("/")
				.maxAge(Duration.ofDays(365))
				.build();
			return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(post);
		}

		return ResponseEntity.ok(post);
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

	private String createViewerKey(Jwt jwt, String viewerCookie, HttpServletRequest request) {
		if (jwt != null) {
			return "member:" + jwt.getSubject();
		}
		return "guest:" + viewerCookie + ":" + getClientIp(request);
	}

	private String getClientIp(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");
		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return forwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
