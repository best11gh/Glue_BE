package org.glue.glue_be.aws.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;


@Configuration
public class S3Config {
	@Value("${cloud.aws.credentials.access-key}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secret-key}")
	private String secretKey;

	@Value("${cloud.aws.region.static}")
	private Region region;


	// AWS 인증 객체 생성 메서드
	private AwsCredentialsProvider getCredentialsProvider() {
		return StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKey, secretKey));
	}

	// 추가용
	@Bean
	public S3Presigner s3Presigner() {
		return S3Presigner.builder()
				.region(region)
				.credentialsProvider(getCredentialsProvider())
				.build();
	}

	// 삭제용
	@Bean
	public S3Client s3Client() {
		return S3Client.builder()
				.region(region)
				.credentialsProvider(getCredentialsProvider())
				.build();
	}
}
