package com.board.server.post;

import java.time.LocalDateTime;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PostViewMapper {

	@Select("""
		SELECT COUNT(*)
		FROM post_views
		WHERE post_id = #{postId}
		  AND viewer_key = #{viewerKey}
		  AND viewed_at >= #{viewedAfter}
		""")
	int countRecentView(
		@Param("postId") Long postId,
		@Param("viewerKey") String viewerKey,
		@Param("viewedAfter") LocalDateTime viewedAfter
	);

	@Insert("""
		INSERT INTO post_views (post_id, viewer_key, viewed_at)
		VALUES (#{postId}, #{viewerKey}, CURRENT_TIMESTAMP)
		ON CONFLICT (post_id, viewer_key)
		DO UPDATE SET viewed_at = CURRENT_TIMESTAMP
		""")
	int saveView(@Param("postId") Long postId, @Param("viewerKey") String viewerKey);
}
