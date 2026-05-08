package com.board.server.member;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/members")
public class MemberController {

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
	public MemberAuthResponse login(@Valid @RequestBody MemberLoginRequest request) {
		return memberService.login(request);
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
		return ResponseEntity.noContent().build();
	}
}
