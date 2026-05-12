package com.board.server.comment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.board.server.post.PostMapper;

@Service
public class CommentService {

	private static final String DELETED_COMMENT_MESSAGE = "삭제된 댓글입니다.";

	private final CommentMapper commentMapper;
	private final PostMapper postMapper;

	public CommentService(CommentMapper commentMapper, PostMapper postMapper) {
		this.commentMapper = commentMapper;
		this.postMapper = postMapper;
	}

	@Transactional(readOnly = true)
	public List<CommentResponse> findByPostId(Long postId) {
		ensurePostExists(postId);
		List<Comment> comments = commentMapper.findByPostId(postId);
		return toTree(comments);
	}

	@Transactional
	public CommentResponse create(Long postId, CommentCreateRequest request, String loginId) {
		ensurePostExists(postId);
		if (request.parentId() != null) {
			Comment parent = findComment(request.parentId());
			if (!parent.getPostId().equals(postId)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "같은 게시글의 댓글에만 답글을 작성할 수 있습니다.");
			}
		}

		Comment comment = new Comment();
		comment.setPostId(postId);
		comment.setParentId(request.parentId());
		comment.setAuthor(loginId);
		comment.setContent(request.content());
		commentMapper.insert(comment);

		return toResponse(findComment(comment.getId()), List.of());
	}

	@Transactional
	public CommentResponse update(Long id, CommentUpdateRequest request, String loginId) {
		Comment comment = findComment(id);
		validateOwner(comment, loginId, "본인이 작성한 댓글만 수정할 수 있습니다.");
		if (comment.isDeleted()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제된 댓글은 수정할 수 없습니다.");
		}

		int updatedRows = commentMapper.updateContent(id, request.content());
		if (updatedRows == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다.");
		}
		return toResponse(findComment(id), List.of());
	}

	@Transactional
	public CommentResponse delete(Long id, String loginId) {
		Comment comment = findComment(id);
		validateOwner(comment, loginId, "본인이 작성한 댓글만 삭제할 수 있습니다.");

		int updatedRows = commentMapper.markDeleted(id);
		if (updatedRows == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다.");
		}
		return toResponse(findComment(id), List.of());
	}

	private void ensurePostExists(Long postId) {
		postMapper.findById(postId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
	}

	private Comment findComment(Long id) {
		return commentMapper.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));
	}

	private void validateOwner(Comment comment, String loginId, String message) {
		if (!comment.getAuthor().equals(loginId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, message);
		}
	}

	private List<CommentResponse> toTree(List<Comment> comments) {
		Map<Long, List<CommentResponse>> repliesByParentId = new LinkedHashMap<>();
		List<CommentResponse> roots = new ArrayList<>();

		for (int index = comments.size() - 1; index >= 0; index--) {
			Comment comment = comments.get(index);
			List<CommentResponse> replies = repliesByParentId.getOrDefault(comment.getId(), List.of());
			CommentResponse response = toResponse(comment, replies);

			if (comment.getParentId() == null) {
				roots.add(0, response);
			} else {
				repliesByParentId
					.computeIfAbsent(comment.getParentId(), key -> new ArrayList<>())
					.add(0, response);
			}
		}

		return roots;
	}

	private CommentResponse toResponse(Comment comment, List<CommentResponse> replies) {
		String content = comment.isDeleted() ? DELETED_COMMENT_MESSAGE : comment.getContent();
		return new CommentResponse(
			comment.getId(),
			comment.getPostId(),
			comment.getParentId(),
			comment.getAuthor(),
			content,
			comment.isDeleted(),
			comment.isEdited(),
			comment.getCreatedAt(),
			comment.getUpdatedAt(),
			replies
		);
	}
}
