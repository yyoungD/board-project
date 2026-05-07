package com.board.server.post;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PostMapper {

	@Select("""
		SELECT id, title, author, content, created_at
		FROM posts
		ORDER BY id DESC
		""")
	List<Post> findAll();

	@Select("""
		SELECT id, title, author, content, created_at
		FROM posts
		WHERE id = #{id}
		""")
	Optional<Post> findById(Long id);

	@Insert("""
		INSERT INTO posts (title, author, content)
		VALUES (#{title}, #{author}, #{content})
		""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(Post post);

	@Update("""
		UPDATE posts
		SET title = #{request.title},
		    author = #{request.author},
		    content = #{request.content}
		WHERE id = #{id}
		""")
	int update(@Param("id") Long id, @Param("request") PostUpdateRequest request);

	@Delete("""
		DELETE FROM posts
		WHERE id = #{id}
		""")
	int deleteById(Long id);
}
