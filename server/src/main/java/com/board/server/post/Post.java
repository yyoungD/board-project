package com.board.server.post;

import java.time.LocalDateTime;
import java.util.List;

import com.board.server.postfile.PostFile;

public class Post {

	private Long id;
	private String title;
	private String author;
	private String content;
	private LocalDateTime createdAt;
	private long commentCount;
	private boolean hasImage;
	private boolean hasFile;
	private long viewCount;
	private List<PostFile> files = List.of();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public long getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(long commentCount) {
		this.commentCount = commentCount;
	}

	public boolean isHasImage() {
		return hasImage;
	}

	public void setHasImage(boolean hasImage) {
		this.hasImage = hasImage;
	}

	public boolean isHasFile() {
		return hasFile;
	}

	public void setHasFile(boolean hasFile) {
		this.hasFile = hasFile;
	}

	public long getViewCount() {
		return viewCount;
	}

	public void setViewCount(long viewCount) {
		this.viewCount = viewCount;
	}

	public List<PostFile> getFiles() {
		return files;
	}

	public void setFiles(List<PostFile> files) {
		this.files = files == null ? List.of() : files;
	}
}
