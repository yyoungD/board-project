package com.board.server.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberSignupRequest(
	@NotBlank
	@Size(min = 4, max = 50)
	@Pattern(regexp = "^[A-Za-z0-9_]+$")
	String loginId,

	@NotBlank
	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{10,}$",
		message = "비밀번호는 10자 이상이며 영어, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다."
	)
	String password,

	@NotBlank
	@Size(max = 100)
	String name,

	@NotBlank
	@Size(max = 30)
	String phone
) {
}
