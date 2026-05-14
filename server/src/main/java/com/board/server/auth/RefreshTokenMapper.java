package com.board.server.auth;

import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RefreshTokenMapper {

	@Insert("""
		INSERT INTO refresh_tokens (member_id, token_hash, expires_at)
		VALUES (#{memberId}, #{tokenHash}, #{expiresAt})
		""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(RefreshToken refreshToken);

	@Select("""
		SELECT id, member_id, token_hash, expires_at, revoked, created_at, last_used_at
		FROM refresh_tokens
		WHERE token_hash = #{tokenHash}
		""")
	Optional<RefreshToken> findByTokenHash(String tokenHash);

	@Update("""
		UPDATE refresh_tokens
		SET revoked = TRUE
		WHERE token_hash = #{tokenHash}
		""")
	int revokeByTokenHash(String tokenHash);

	@Update("""
		UPDATE refresh_tokens
		SET revoked = TRUE
		WHERE member_id = #{memberId}
		""")
	int revokeByMemberId(Long memberId);

	@Update("""
		UPDATE refresh_tokens
		SET last_used_at = #{lastUsedAt}
		WHERE id = #{id}
		""")
	int updateLastUsedAt(@Param("id") Long id, @Param("lastUsedAt") LocalDateTime lastUsedAt);
}
