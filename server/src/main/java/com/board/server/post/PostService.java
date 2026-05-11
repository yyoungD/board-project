package com.board.server.post;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.board.server.config.S3Properties;
import com.board.server.postfile.PostFile;
import com.board.server.postfile.PostFileMapper;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class PostService {

	private static final Pattern IMAGE_URL_PATTERN = Pattern.compile("/api/uploads/images/(\\d+)");

	private final PostMapper postMapper;
	private final PostFileMapper postFileMapper;
	private final S3Client s3Client;
	private final S3Properties s3Properties;

	public PostService(
		PostMapper postMapper,
		PostFileMapper postFileMapper,
		S3Client s3Client,
		S3Properties s3Properties
	) {
		this.postMapper = postMapper;
		this.postFileMapper = postFileMapper;
		this.s3Client = s3Client;
		this.s3Properties = s3Properties;
	}

	@Transactional(readOnly = true)
	public PageResponse<Post> findPage(int page, int size, String keyword) {
		int safePage = Math.max(page, 1);
		int safeSize = Math.min(Math.max(size, 1), 100);
		int offset = (safePage - 1) * safeSize;
		String trimmedKeyword = keyword == null ? "" : keyword.trim();

		List<Post> content = postMapper.findPage(trimmedKeyword, safeSize, offset);
		long totalElements = postMapper.countAll(trimmedKeyword);
		int totalPages = (int) Math.ceil((double) totalElements / safeSize);

		return new PageResponse<>(content, safePage, safeSize, totalElements, totalPages);
	}

	@Transactional(readOnly = true)
	public Post findById(Long id) {
		return postMapper.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
	}

	@Transactional
	public Post create(PostCreateRequest request, String loginId) {
		Post post = new Post();
		post.setTitle(request.title());
		post.setAuthor(loginId);
		post.setContent(request.content());

		postMapper.insert(post);
		attachImages(post.getId(), request.content());
		return findById(post.getId());
	}

	@Transactional
	public Post update(Long id, PostUpdateRequest request, String loginId) {
		Post post = findById(id);
		if (!post.getAuthor().equals(loginId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 글만 수정할 수 있습니다.");
		}

		int updatedRows = postMapper.update(id, request);
		if (updatedRows == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
		}
		postFileMapper.detachByPostId(id);
		attachImages(id, request.content());
		return findById(id);
	}

	@Transactional
	public void deleteById(Long id, String loginId) {
		Post post = findById(id);
		if (!post.getAuthor().equals(loginId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 글만 삭제할 수 있습니다.");
		}

		List<String> imagePaths = postFileMapper.findByPostId(id).stream()
			.map(PostFile::getFilePath)
			.toList();

		int deletedRows = postMapper.deleteById(id);
		if (deletedRows == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
		}

		deleteImagesFromS3(imagePaths);
	}

	private void attachImages(Long postId, String content) {
		List<Long> fileIds = extractImageFileIds(content);
		if (!fileIds.isEmpty()) {
			postFileMapper.attachToPost(postId, fileIds);
		}
	}

	private List<Long> extractImageFileIds(String content) {
		LinkedHashSet<Long> fileIds = new LinkedHashSet<>();
		Matcher matcher = IMAGE_URL_PATTERN.matcher(content == null ? "" : content);
		while (matcher.find()) {
			fileIds.add(Long.valueOf(matcher.group(1)));
		}
		return List.copyOf(fileIds);
	}

	private void deleteImagesFromS3(List<String> imagePaths) {
		for (String imagePath : imagePaths) {
			try {
				DeleteObjectRequest request = DeleteObjectRequest.builder()
					.bucket(s3Properties.bucket())
					.key(imagePath)
					.build();
				s3Client.deleteObject(request);
			} catch (S3Exception exception) {
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제에 실패했습니다.");
			}
		}
	}
}
