package com.board.server.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberUpdateRequest(
	@NotBlank(message = "전화번호를 입력해 주세요.")
	@Size(max = 30, message = "전화번호는 30자 이하로 입력해 주세요.")
	String phone,

	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{10,}$",
		message = "비밀번호는 10자 이상이며 영문, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다."
	)
	String password
) {
	public boolean hasPassword() {
		return password != null && !password.isBlank();
	}
}
