package com.board.server.member;

import com.board.server.auth.IssuedRefreshToken;

public record MemberAuthResult(
	MemberAuthResponse response,
	IssuedRefreshToken refreshToken
) {
}
