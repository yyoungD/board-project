package com.board.server.member;

public record MemberAuthResponse(
	MemberSignupResponse member,
	String token
) {
}
