package com.board.server.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberSignupRequest(
	@NotBlank(message = "아이디를 입력해 주세요.")
	@Size(min = 4, max = 50, message = "아이디는 4자 이상 50자 이하로 입력해 주세요.")
	@Pattern(regexp = "^[A-Za-z0-9_]+$", message = "아이디는 영문, 숫자, 밑줄만 사용할 수 있습니다.")
	String loginId,

	@NotBlank(message = "비밀번호를 입력해 주세요.")
	@Pattern(
		regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{10,}$",
		message = "비밀번호는 10자 이상이며 영문, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다."
	)
	String password,

	@NotBlank(message = "이름을 입력해 주세요.")
	@Size(max = 100, message = "이름은 100자 이하로 입력해 주세요.")
	String name,

	@NotBlank(message = "전화번호를 입력해 주세요.")
	@Size(max = 30, message = "전화번호는 30자 이하로 입력해 주세요.")
	String phone
) {
}
