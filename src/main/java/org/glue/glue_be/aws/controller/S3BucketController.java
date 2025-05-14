package org.glue.glue_be.aws.controller;

import lombok.RequiredArgsConstructor;
import org.glue.glue_be.auth.jwt.CustomUserDetails;
import org.glue.glue_be.aws.dto.GetPresignedUrlResponse;
import org.glue.glue_be.aws.service.FileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/aws/presigned-url")
public class S3BucketController {

	private final FileService fileService;


	// bucketObject: S3 버킷의 폴더명 지정 -> post_images or profile_images
	// extension: 파일의 확장자
	@PostMapping
	public GetPresignedUrlResponse getPresignedUrl(
		@RequestParam String bucketObject,
		@RequestParam String extension,
		@AuthenticationPrincipal CustomUserDetails auth) {

		return fileService.getPreSignedUrl(bucketObject, extension, auth.getUserNickname());
	}
}