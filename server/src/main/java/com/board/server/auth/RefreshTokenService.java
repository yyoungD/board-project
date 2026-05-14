package com.board.server.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.board.server.config.JwtProperties;

@Service
public class RefreshTokenService {

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private final RefreshTokenMapper refreshTokenMapper;
	private final long refreshExpirationSeconds;

	public RefreshTokenService(RefreshTokenMapper refreshTokenMapper, JwtProperties jwtProperties) {
		this.refreshTokenMapper = refreshTokenMapper;
		this.refreshExpirationSeconds = jwtProperties.refreshExpirationSeconds();
	}

	@Transactional
	public IssuedRefreshToken issue(Long memberId) {
		String token = createRandomToken();
		LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshExpirationSeconds);

		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setMemberId(memberId);
		refreshToken.setTokenHash(hash(token));
		refreshToken.setExpiresAt(expiresAt);
		refreshTokenMapper.insert(refreshToken);

		return new IssuedRefreshToken(token, expiresAt);
	}

	@Transactional
	public RefreshToken validateAndRotate(String token) {
		RefreshToken refreshToken = refreshTokenMapper.findByTokenHash(hash(token))
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is invalid."));

		if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
			refreshTokenMapper.revokeByTokenHash(refreshToken.getTokenHash());
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is expired.");
		}

		refreshTokenMapper.updateLastUsedAt(refreshToken.getId(), LocalDateTime.now());
		refreshTokenMapper.revokeByTokenHash(refreshToken.getTokenHash());
		return refreshToken;
	}

	@Transactional
	public void revoke(String token) {
		if (token != null && !token.isBlank()) {
			refreshTokenMapper.revokeByTokenHash(hash(token));
		}
	}

	@Transactional
	public void revokeByMemberId(Long memberId) {
		refreshTokenMapper.revokeByMemberId(memberId);
	}

	private String createRandomToken() {
		byte[] bytes = new byte[64];
		SECURE_RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private String hash(String token) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
			StringBuilder builder = new StringBuilder(hashed.length * 2);
			for (byte value : hashed) {
				builder.append(String.format("%02x", value));
			}
			return builder.toString();
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 algorithm is not available.", exception);
		}
	}
}
