package org.glue.glue_be.aws.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.auth.jwt.CustomUserDetails;
import org.glue.glue_be.aws.dto.GetPresignedUrlResponse;
import org.glue.glue_be.aws.service.FileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/aws/presigned-url")
@Tag(name = "AWS S3", description = "AWS S3 Presigned URL 관련 API")
public class S3BucketController {

    private final FileService fileService;


    // bucketObject: S3 버킷의 폴더명 지정 -> post_images or profile_images
    // extension: 파일의 확장자
    @PostMapping
    @Operation(summary = "S3 Presigned URL 발급")
    public GetPresignedUrlResponse getPresignedUrl(
            @Parameter(description = "S3 버킷의 폴더명 (예: post_images, profile_images)")
            @RequestParam String bucketObject,

            @Parameter(description = "파일 확장자 (예: jpg, png)")
            @RequestParam String extension,

            @AuthenticationPrincipal CustomUserDetails auth
    ) {
        return fileService.getPreSignedUrl(bucketObject, extension, auth.getUserNickname());
    }
}