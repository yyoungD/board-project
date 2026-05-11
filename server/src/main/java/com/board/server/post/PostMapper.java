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
		SELECT p.id, p.title, p.author, p.content, p.created_at, p.view_count,
		       COUNT(c.id) AS comment_count,
		       EXISTS (
		           SELECT 1
		           FROM files f
		           WHERE f.post_id = p.id
		       ) AS has_image
		FROM posts p
		LEFT JOIN comments c
		  ON c.post_id = p.id
		 AND c.deleted = FALSE
		<where>
			<if test="keyword != null and keyword != ''">
				p.title ILIKE CONCAT('%', #{keyword}, '%')
			</if>
		</where>
		GROUP BY p.id
		ORDER BY p.id DESC
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
		SELECT p.id, p.title, p.author, p.content, p.created_at, p.view_count,
		       COUNT(c.id) AS comment_count,
		       EXISTS (
		           SELECT 1
		           FROM files f
		           WHERE f.post_id = p.id
		       ) AS has_image
		FROM posts p
		LEFT JOIN comments c
		  ON c.post_id = p.id
		 AND c.deleted = FALSE
		WHERE p.id = #{id}
		GROUP BY p.id
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

	@Update("""
		UPDATE posts
		SET view_count = view_count + 1
		WHERE id = #{id}
		""")
	int incrementViewCount(Long id);
}
