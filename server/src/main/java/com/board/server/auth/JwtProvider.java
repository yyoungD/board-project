package com.board.server.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtProvider {

	private static final String HMAC_SHA256 = "HmacSHA256";
	private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

	private final ObjectMapper objectMapper;
	private final byte[] secret;
	private final long expirationSeconds;

	public JwtProvider(
		ObjectMapper objectMapper,
		@Value("${jwt.secret}") String secret,
		@Value("${jwt.expiration-seconds}") long expirationSeconds
	) {
		this.objectMapper = objectMapper;
		this.secret = secret.getBytes(StandardCharsets.UTF_8);
		this.expirationSeconds = expirationSeconds;
	}

	public String createToken(AuthenticatedMember member) {
		long now = Instant.now().getEpochSecond();

		Map<String, Object> header = new LinkedHashMap<>();
		header.put("alg", "HS256");
		header.put("typ", "JWT");

		Map<String, Object> payload = new LinkedHashMap<>();
		payload.put("sub", member.loginId());
		payload.put("memberId", member.id());
		payload.put("name", member.name());
		payload.put("iat", now);
		payload.put("exp", now + expirationSeconds);

		String encodedHeader = encodeJson(header);
		String encodedPayload = encodeJson(payload);
		String unsignedToken = encodedHeader + "." + encodedPayload;

		return unsignedToken + "." + sign(unsignedToken);
	}

	public AuthenticatedMember validateToken(String token) {
		try {
			String[] parts = token.split("\\.");
			if (parts.length != 3) {
				throw unauthorized();
			}

			String unsignedToken = parts[0] + "." + parts[1];
			if (!constantTimeEquals(sign(unsignedToken), parts[2])) {
				throw unauthorized();
			}

			Map<String, Object> payload = objectMapper.readValue(
				URL_DECODER.decode(parts[1]),
				new TypeReference<>() {
				}
			);

			long exp = ((Number) payload.get("exp")).longValue();
			if (Instant.now().getEpochSecond() >= exp) {
				throw unauthorized();
			}

			return new AuthenticatedMember(
				((Number) payload.get("memberId")).longValue(),
				(String) payload.get("sub"),
				(String) payload.get("name")
			);
		} catch (ResponseStatusException exception) {
			throw exception;
		} catch (Exception exception) {
			throw unauthorized();
		}
	}

	private String encodeJson(Map<String, Object> value) {
		try {
			return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to create token", exception);
		}
	}

	private String sign(String value) {
		try {
			Mac mac = Mac.getInstance(HMAC_SHA256);
			mac.init(new SecretKeySpec(secret, HMAC_SHA256));
			return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to sign token", exception);
		}
	}

	private boolean constantTimeEquals(String left, String right) {
		byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
		byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
		if (leftBytes.length != rightBytes.length) {
			return false;
		}

		int result = 0;
		for (int i = 0; i < leftBytes.length; i++) {
			result |= leftBytes[i] ^ rightBytes[i];
		}
		return result == 0;
	}

	private ResponseStatusException unauthorized() {
		return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
	}
}
