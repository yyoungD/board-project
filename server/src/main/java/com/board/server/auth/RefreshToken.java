package com.board.server.auth;

import java.time.LocalDateTime;

public class RefreshToken {

	private Long id;
	private Long memberId;
	private String tokenHash;
	private LocalDateTime expiresAt;
	private boolean revoked;
	private LocalDateTime createdAt;
	private LocalDateTime lastUsedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public String getTokenHash() {
		return tokenHash;
	}

	public void setTokenHash(String tokenHash) {
		this.tokenHash = tokenHash;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	public boolean isRevoked() {
		return revoked;
	}

	public void setRevoked(boolean revoked) {
		this.revoked = revoked;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getLastUsedAt() {
		return lastUsedAt;
	}

	public void setLastUsedAt(LocalDateTime lastUsedAt) {
		this.lastUsedAt = lastUsedAt;
	}
}
