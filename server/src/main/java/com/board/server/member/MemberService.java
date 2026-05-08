package com.board.server.member;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.board.server.auth.AuthenticatedMember;
import com.board.server.auth.JwtProvider;

@Service
public class MemberService {

	private final MemberMapper memberMapper;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;

	public MemberService(MemberMapper memberMapper, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
		this.memberMapper = memberMapper;
		this.passwordEncoder = passwordEncoder;
		this.jwtProvider = jwtProvider;
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
	public MemberAuthResponse login(MemberLoginRequest request) {
		Member member = memberMapper.findByLoginId(request.loginId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."));

		if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다.");
		}

		MemberSignupResponse memberResponse = MemberSignupResponse.from(member);
		String token = jwtProvider.createToken(new AuthenticatedMember(member.getId(), member.getLoginId(), member.getName()));

		return new MemberAuthResponse(memberResponse, token);
	}

	@Transactional(readOnly = true)
	public MemberSignupResponse findMe(String loginId) {
		return memberMapper.findByLoginId(loginId)
			.map(MemberSignupResponse::from)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
	}

	@Transactional
	public MemberAuthResponse updateMe(String loginId, MemberUpdateRequest request) {
		int updatedRows = memberMapper.updateProfile(loginId, request.phone());
		if (updatedRows == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다.");
		}

		if (request.hasPassword()) {
			memberMapper.updatePassword(loginId, passwordEncoder.encode(request.password()));
		}

		Member member = memberMapper.findByLoginId(loginId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));

		MemberSignupResponse memberResponse = MemberSignupResponse.from(member);
		String token = jwtProvider.createToken(new AuthenticatedMember(member.getId(), member.getLoginId(), member.getName()));

		return new MemberAuthResponse(memberResponse, token);
	}

	@Transactional
	public void deleteMe(String loginId) {
		int deletedRows = memberMapper.deleteByLoginId(loginId);
		if (deletedRows == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다.");
		}
	}
}
