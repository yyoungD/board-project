package com.board.server.member;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.board.server.auth.IssuedRefreshToken;

@RestController
@RequestMapping("/api/members")
public class MemberController {

	private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
	private static final String REFRESH_TOKEN_COOKIE_PATH = "/api/members";

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	@PostMapping("/signup")
	public ResponseEntity<MemberSignupResponse> signup(@Valid @RequestBody MemberSignupRequest request) {
		MemberSignupResponse response = memberService.signup(request);
		return ResponseEntity
			.created(URI.create("/api/members/" + response.id()))
			.body(response);
	}

	@PostMapping("/login")
	public ResponseEntity<MemberAuthResponse> login(@Valid @RequestBody MemberLoginRequest request) {
		MemberAuthResult result = memberService.login(request);
		return authResponse(result);
	}

	@PostMapping("/refresh")
	public ResponseEntity<MemberAuthResponse> refresh(
		@CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken
	) {
		MemberAuthResult result = memberService.refresh(refreshToken);
		return authResponse(result);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(
		@CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken
	) {
		memberService.logout(refreshToken);
		return ResponseEntity.noContent()
			.header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
			.build();
	}

	@GetMapping("/me")
	public MemberSignupResponse findMe(@AuthenticationPrincipal Jwt jwt) {
		return memberService.findMe(jwt.getSubject());
	}

	@PutMapping("/me")
	public MemberAuthResponse updateMe(
		@AuthenticationPrincipal Jwt jwt,
		@Valid @RequestBody MemberUpdateRequest request
	) {
		return memberService.updateMe(jwt.getSubject(), request);
	}

	@DeleteMapping("/me")
	public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal Jwt jwt) {
		memberService.deleteMe(jwt.getSubject());
		return ResponseEntity.noContent()
			.header(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString())
			.build();
	}

	private ResponseEntity<MemberAuthResponse> authResponse(MemberAuthResult result) {
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken()).toString())
			.body(result.response());
	}

	private ResponseCookie refreshCookie(IssuedRefreshToken refreshToken) {
		long maxAgeSeconds = Math.max(0, Duration.between(LocalDateTime.now(), refreshToken.expiresAt()).toSeconds());
		return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken.token())
			.httpOnly(true)
			.secure(false)
			.sameSite("Lax")
			.path(REFRESH_TOKEN_COOKIE_PATH)
			.maxAge(Duration.ofSeconds(maxAgeSeconds))
			.build();
	}

	private ResponseCookie clearRefreshCookie() {
		return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
			.httpOnly(true)
			.secure(false)
			.sameSite("Lax")
			.path(REFRESH_TOKEN_COOKIE_PATH)
			.maxAge(Duration.ZERO)
			.build();
	}
}
