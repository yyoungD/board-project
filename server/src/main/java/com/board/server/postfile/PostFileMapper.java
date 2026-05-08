package com.board.server.postfile;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostFileMapper {

	@Select("""
		SELECT id, post_id, original_name, stored_name, file_path, content_type, file_size, created_at
		FROM files
		WHERE post_id = #{postId}
		ORDER BY id ASC
		""")
	List<PostFile> findByPostId(Long postId);

	@Insert("""
		INSERT INTO files (post_id, original_name, stored_name, file_path, content_type, file_size)
		VALUES (#{postId}, #{originalName}, #{storedName}, #{filePath}, #{contentType}, #{fileSize})
		""")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	int insert(PostFile postFile);

	@Delete("""
		DELETE FROM files
		WHERE post_id = #{postId}
		""")
	int deleteByPostId(Long postId);
}
