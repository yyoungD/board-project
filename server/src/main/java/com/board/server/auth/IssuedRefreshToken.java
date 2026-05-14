package com.board.server.auth;

import java.time.LocalDateTime;

public record IssuedRefreshToken(
	String token,
	LocalDateTime expiresAt
) {
}
