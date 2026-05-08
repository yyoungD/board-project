package com.board.server.member;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MemberService {

	private final MemberMapper memberMapper;
	private final PasswordEncoder passwordEncoder;

	public MemberService(MemberMapper memberMapper, PasswordEncoder passwordEncoder) {
		this.memberMapper = memberMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public MemberSignupResponse signup(MemberSignupRequest request) {
		if (memberMapper.existsByLoginId(request.loginId())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 아이디입니다.");
		}

		Member member = new Member();
		member.setLoginId(request.loginId());
		member.setPasswordHash(passwordEncoder.encode(request.password()));
		member.setName(request.name());
		member.setPhone(request.phone());

		memberMapper.insert(member);

		return memberMapper.findById(member.getId())
			.map(MemberSignupResponse::from)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "회원 저장에 실패했습니다."));
	}

	@Transactional(readOnly = true)
	public MemberSignupResponse login(MemberLoginRequest request) {
		Member member = memberMapper.findByLoginId(request.loginId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."));

		if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");
		}

		return MemberSignupResponse.from(member);
	}
}
