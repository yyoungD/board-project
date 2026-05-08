package com.board.server.auth;

import java.time.Instant;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import com.board.server.config.JwtProperties;

@Component
public class JwtProvider {

	private final JwtEncoder jwtEncoder;
	private final long expirationSeconds;

	public JwtProvider(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
		this.jwtEncoder = jwtEncoder;
		this.expirationSeconds = jwtProperties.expirationSeconds();
	}

	public String createToken(AuthenticatedMember member) {
		Instant now = Instant.now();

		JwtClaimsSet claims = JwtClaimsSet.builder()
			.subject(member.loginId())
			.claim("memberId", member.id())
			.claim("name", member.name())
			.issuedAt(now)
			.expiresAt(now.plusSeconds(expirationSeconds))
			.build();

		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

		return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
	}
}
