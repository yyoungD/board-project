package com.board.server.auth;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.board.server.member.MemberAuthResult;
import com.board.server.member.MemberService;
import com.board.server.member.MemberSignupResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
	private static final String REFRESH_TOKEN_COOKIE_PATH = "/api/members";

	private final MemberService memberService;
	private final String redirectSuccessUrl;

	public OAuth2LoginSuccessHandler(
		MemberService memberService,
		@Value("${app.oauth2.redirect-success-url:http://localhost:3000/oauth2/redirect}") String redirectSuccessUrl
	) {
		this.memberService = memberService;
		this.redirectSuccessUrl = redirectSuccessUrl;
	}

	@Override
	public void onAuthenticationSuccess(
		HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication
	) throws IOException, ServletException {
		OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
		MemberAuthResult result = memberService.loginWithGoogle(oauth2User);
		MemberSignupResponse member = result.response().member();

		response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie(result.refreshToken()).toString());
		response.sendRedirect(UriComponentsBuilder
			.fromUriString(redirectSuccessUrl)
			.queryParam("token", result.response().token())
			.queryParam("memberId", member.id())
			.queryParam("loginId", member.loginId())
			.queryParam("name", member.name())
			.queryParam("phone", member.phone())
			.queryParam("createdAt", member.createdAt())
			.build()
			.encode()
			.toUriString());
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
}
