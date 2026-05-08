package com.board.server.comment;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CommentMapper {

	@Select("""
		SELECT id, post_id, parent_id, author, content, deleted, edited, created_at, updated_at
		FROM comments
		WHERE post_id = #{postId}
		ORDER BY created_at ASC, id ASC
		""")
	List<Comment> findByPostId(Long postId);

	@Select("""
		SELECT id, post_id, parent_id, author, content, deleted, edited, created_at, updated_at
		FROM comments
		WHERE id = #{id}
		""")
	Optional<Comment> findById(Long id);

	@Insert("""
		INSERT INTO comments (post_id, parent_id, author, content)
		VALUES (#{postId}, #{parentId}, #{author}, #{content})
		""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(Comment comment);

	@Update("""
		UPDATE comments
		SET content = #{content},
		    edited = TRUE,
		    updated_at = CURRENT_TIMESTAMP
		WHERE id = #{id}
		  AND deleted = FALSE
		""")
	int updateContent(@Param("id") Long id, @Param("content") String content);

	@Update("""
		UPDATE comments
		SET content = '삭제된 댓글입니다.',
		    deleted = TRUE,
		    updated_at = CURRENT_TIMESTAMP
		WHERE id = #{id}
		  AND deleted = FALSE
		""")
	int markDeleted(Long id);
}
