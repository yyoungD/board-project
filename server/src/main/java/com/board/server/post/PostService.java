package com.board.server.post;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.board.server.config.S3Properties;
import com.board.server.postfile.PostFile;
import com.board.server.postfile.PostFileMapper;
import com.board.server.upload.FileUploadService;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
public class PostService {

	private static final Pattern IMAGE_URL_PATTERN = Pattern.compile("/api/uploads/images/(\\d+)");
	private static final int VIEW_COUNT_INTERVAL_HOURS = 24;

	private final PostMapper postMapper;
	private final PostViewMapper postViewMapper;
	private final PostFileMapper postFileMapper;
	private final S3Client s3Client;
	private final S3Properties s3Properties;
	private final FileUploadService fileUploadService;

	public PostService(
		PostMapper postMapper,
		PostViewMapper postViewMapper,
		PostFileMapper postFileMapper,
		S3Client s3Client,
		S3Properties s3Properties,
		FileUploadService fileUploadService
	) {
		this.postMapper = postMapper;
		this.postViewMapper = postViewMapper;
		this.postFileMapper = postFileMapper;
		this.s3Client = s3Client;
		this.s3Properties = s3Properties;
		this.fileUploadService = fileUploadService;
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
		Post post = postMapper.findById(id)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));
		post.setFiles(postFileMapper.findAttachmentsByPostId(id));
		return post;
	}

	@Transactional
	public Post findByIdAndCountView(Long id, String viewerKey) {
		Post post = findById(id);
		LocalDateTime viewedAfter = LocalDateTime.now().minusHours(VIEW_COUNT_INTERVAL_HOURS);
		boolean recentlyViewed = postViewMapper.countRecentView(id, viewerKey, viewedAfter) > 0;

		if (!recentlyViewed) {
			postViewMapper.saveView(id, viewerKey);
			postMapper.incrementViewCount(id);
			return findById(id);
		}

		return post;
	}

	@Transactional
	public Post create(PostCreateRequest request, String loginId) {
		Post post = new Post();
		post.setTitle(request.title());
		post.setAuthor(loginId);
		post.setContent(request.content());

		postMapper.insert(post);
		attachImages(post.getId(), request.content());
		attachFiles(post.getId(), request.fileIds());
		return findById(post.getId());
	}

	@Transactional
	public Post update(Long id, PostUpdateRequest request, String loginId) {
		Post post = findById(id);
		if (!post.getAuthor().equals(loginId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 작성한 글만 수정할 수 있습니다.");
		}
		List<PostFile> currentAttachments = postFileMapper.findAttachmentsByPostId(id);
		List<Long> attachmentFileIds = request.fileIds() == null
			? currentAttachments.stream().map(PostFile::getId).toList()
			: request.fileIds();
		Set<Long> nextAttachmentFileIds = Set.copyOf(attachmentFileIds);
		List<PostFile> removedAttachments = currentAttachments.stream()
			.filter((file) -> !nextAttachmentFileIds.contains(file.getId()))
			.toList();

		int updatedRows = postMapper.update(id, request);
		if (updatedRows == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
		}
		postFileMapper.detachByPostId(id);
		attachImages(id, request.content());
		attachFiles(id, attachmentFileIds);
		fileUploadService.deleteFiles(removedAttachments);
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
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다.");
		}

		deleteImagesFromS3(imagePaths);
	}

	private void attachImages(Long postId, String content) {
		List<Long> fileIds = extractImageFileIds(content);
		if (!fileIds.isEmpty()) {
			postFileMapper.attachToPost(postId, fileIds);
		}
	}

	private void attachFiles(Long postId, List<Long> fileIds) {
		if (fileIds != null && !fileIds.isEmpty()) {
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
			} catch (SdkException exception) {
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제에 실패했습니다.");
			}
		}
	}
}
