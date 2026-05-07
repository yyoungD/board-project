package com.board.server.post;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PostService {

	private final PostMapper postMapper;

	public PostService(PostMapper postMapper) {
		this.postMapper = postMapper;
	}

	@Transactional(readOnly = true)
	public List<Post> findAll() {
		return postMapper.findAll();
	}

	@Transactional(readOnly = true)
	public Post findById(Long id) {
		return postMapper.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
	}

	@Transactional
	public Post create(PostCreateRequest request) {
		Post post = new Post();
		post.setTitle(request.title());
		post.setAuthor(request.author());
		post.setContent(request.content());

		postMapper.insert(post);
		return findById(post.getId());
	}

	@Transactional
	public Post update(Long id, PostUpdateRequest request) {
		int updatedRows = postMapper.update(id, request);
		if (updatedRows == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
		}
		return findById(id);
	}

	@Transactional
	public void deleteById(Long id) {
		int deletedRows = postMapper.deleteById(id);
		if (deletedRows == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
		}
	}
}
