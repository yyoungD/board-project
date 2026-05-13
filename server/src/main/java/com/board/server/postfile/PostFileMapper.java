package com.board.server.postfile;

import java.time.LocalDateTime;
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
public interface PostFileMapper {

	@Select("""
		SELECT id, post_id, original_name, stored_name, file_path, content_type, file_size, created_at
		FROM files
		WHERE post_id = #{postId}
		ORDER BY id ASC
		""")
	List<PostFile> findByPostId(Long postId);

	@Select("""
		SELECT id, post_id, original_name, stored_name, file_path, content_type, file_size, created_at
		FROM files
		WHERE post_id = #{postId}
		  AND file_path LIKE 'files/%'
		ORDER BY id ASC
	""")
	List<PostFile> findAttachmentsByPostId(Long postId);

	@Select("""
		SELECT id, post_id, original_name, stored_name, file_path, content_type, file_size, created_at
		FROM files
		WHERE post_id IS NULL
		  AND (file_path LIKE 'files/%' OR file_path LIKE 'images/%')
		  AND created_at < #{createdBefore}
		ORDER BY id ASC
		""")
	List<PostFile> findUnattachedFilesCreatedBefore(LocalDateTime createdBefore);

	@Select("""
		SELECT id, post_id, original_name, stored_name, file_path, content_type, file_size, created_at
		FROM files
		WHERE id = #{id}
		""")
	Optional<PostFile> findById(Long id);

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

	@Delete("""
		DELETE FROM files
		WHERE id = #{id}
		""")
	int deleteById(Long id);

	@Update("""
		UPDATE files
		SET post_id = NULL
		WHERE post_id = #{postId}
		""")
	int detachByPostId(Long postId);

	@Update("""
		<script>
		UPDATE files
		SET post_id = #{postId}
		WHERE id IN
		<foreach collection="fileIds" item="fileId" open="(" separator="," close=")">
			#{fileId}
		</foreach>
		AND (post_id IS NULL OR post_id = #{postId})
		</script>
		""")
	int attachToPost(@Param("postId") Long postId, @Param("fileIds") List<Long> fileIds);
}
