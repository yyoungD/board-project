package com.board.server.member;

import java.util.Optional;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

	@Select("""
		SELECT id, login_id, password_hash, name, phone, created_at
		FROM members
		WHERE login_id = #{loginId}
		""")
	Optional<Member> findByLoginId(String loginId);

	@Insert("""
		INSERT INTO members (login_id, password_hash, name, phone)
		VALUES (#{loginId}, #{passwordHash}, #{name}, #{phone})
		""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(Member member);

	@Update("""
		UPDATE members
		SET phone = #{phone}
		WHERE login_id = #{loginId}
		""")
	int updateProfile(@Param("loginId") String loginId, @Param("phone") String phone);

	@Update("""
		UPDATE members
		SET password_hash = #{passwordHash}
		WHERE login_id = #{loginId}
		""")
	int updatePassword(@Param("loginId") String loginId, @Param("passwordHash") String passwordHash);

	@Delete("""
		DELETE FROM members
		WHERE login_id = #{loginId}
		""")
	int deleteByLoginId(String loginId);
}
