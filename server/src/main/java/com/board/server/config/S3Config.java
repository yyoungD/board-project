package com.board.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

	@Bean
	public S3Client s3Client(S3Properties s3Properties) {
		return S3Client.builder()
			.region(Region.of(s3Properties.region()))
			.credentialsProvider(credentialsProvider(s3Properties))
			.build();
	}

	private AwsCredentialsProvider credentialsProvider(S3Properties s3Properties) {
		if (!StringUtils.hasText(s3Properties.accessKeyId()) || !StringUtils.hasText(s3Properties.secretAccessKey())) {
			return DefaultCredentialsProvider.builder().build();
		}

		if (StringUtils.hasText(s3Properties.sessionToken())) {
			return StaticCredentialsProvider.create(AwsSessionCredentials.create(
				s3Properties.accessKeyId(),
				s3Properties.secretAccessKey(),
				s3Properties.sessionToken()
			));
		}

		return StaticCredentialsProvider.create(AwsBasicCredentials.create(
			s3Properties.accessKeyId(),
			s3Properties.secretAccessKey()
		));
	}
}
