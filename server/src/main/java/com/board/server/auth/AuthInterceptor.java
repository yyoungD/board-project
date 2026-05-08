package com.board.server.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

	public static final String AUTHENTICATED_MEMBER_ATTRIBUTE = "authenticatedMember";

	private final JwtProvider jwtProvider;

	public AuthInterceptor(JwtProvider jwtProvider) {
		this.jwtProvider = jwtProvider;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (!requiresAuthentication(request)) {
			return true;
		}

		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
		}

		AuthenticatedMember member = jwtProvider.validateToken(authorization.substring(7));
		request.setAttribute(AUTHENTICATED_MEMBER_ATTRIBUTE, member);
		return true;
	}

	private boolean requiresAuthentication(HttpServletRequest request) {
		String path = request.getRequestURI();
		String method = request.getMethod();

		if (!path.startsWith("/api/posts")) {
			return false;
		}

		return HttpMethod.POST.matches(method) || HttpMethod.PUT.matches(method) || HttpMethod.DELETE.matches(method);
	}
}
