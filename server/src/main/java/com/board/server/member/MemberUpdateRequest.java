package com.board.server.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberUpdateRequest(
	@NotBlank
	@Size(max = 30)
	String phone,

	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{10,}$",
		message = "비밀번호는 10자 이상이며 영어, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다."
	)
	String password
) {
	public boolean hasPassword() {
		return password != null && !password.isBlank();
	}
}
