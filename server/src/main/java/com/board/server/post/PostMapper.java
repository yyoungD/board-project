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
		<script>
		SELECT id, title, author, content, created_at
		FROM posts
		<where>
			<if test="keyword != null and keyword != ''">
				title ILIKE CONCAT('%', #{keyword}, '%')
			</if>
		</where>
		ORDER BY id DESC
		LIMIT #{size}
		OFFSET #{offset}
		</script>
		""")
	List<Post> findPage(
		@Param("keyword") String keyword,
		@Param("size") int size,
		@Param("offset") int offset
	);

	@Select("""
		<script>
		SELECT COUNT(*)
		FROM posts
		<where>
			<if test="keyword != null and keyword != ''">
				title ILIKE CONCAT('%', #{keyword}, '%')
			</if>
		</where>
		</script>
		""")
	long countAll(@Param("keyword") String keyword);

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
