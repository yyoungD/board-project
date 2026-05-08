package com.board.server.member;

import java.time.LocalDateTime;

public record MemberSignupResponse(
	Long id,
	String loginId,
	String name,
	String phone,
	LocalDateTime createdAt
) {
	public static MemberSignupResponse from(Member member) {
		return new MemberSignupResponse(
			member.getId(),
			member.getLoginId(),
			member.getName(),
			member.getPhone(),
			member.getCreatedAt()
		);
	}
}
