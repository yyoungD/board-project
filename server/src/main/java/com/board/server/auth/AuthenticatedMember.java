package com.board.server.auth;

public record AuthenticatedMember(
	Long id,
	String loginId,
	String name
) {
}
