package com.board.server.upload;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class FileCleanupScheduler {

	private static final Logger log = LoggerFactory.getLogger(FileCleanupScheduler.class);

	private final FileUploadService fileUploadService;
	private final Duration maxAge;

	public FileCleanupScheduler(
		FileUploadService fileUploadService,
		@Value("${uploads.cleanup.unattached-max-age-hours:24}") long maxAgeHours
	) {
		this.fileUploadService = fileUploadService;
		this.maxAge = Duration.ofHours(maxAgeHours);
	}

	@Scheduled(
		initialDelayString = "${uploads.cleanup.initial-delay-ms:600000}",
		fixedDelayString = "${uploads.cleanup.fixed-delay-ms:3600000}"
	)
	public void cleanupUnattachedFiles() {
		int deletedCount = fileUploadService.cleanupUnattachedFiles(maxAge);
		if (deletedCount > 0) {
			log.info("Cleaned up {} unattached upload files.", deletedCount);
		}
	}
}
