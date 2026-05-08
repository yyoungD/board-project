package com.board.server.member;

import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MemberMapper {

	@Select("""
		SELECT COUNT(*) > 0
		FROM members
		WHERE login_id = #{loginId}
		""")
	boolean existsByLoginId(String loginId);

	@Select("""
		SELECT id, login_id, password_hash, name, phone, created_at
		FROM members
		WHERE id = #{id}
		""")
	Optional<Member> findById(Long id);

	@Insert("""
		INSERT INTO members (login_id, password_hash, name, phone)
		VALUES (#{loginId}, #{passwordHash}, #{name}, #{phone})
		""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(Member member);
}
