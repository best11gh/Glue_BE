package org.glue.glue_be.aws.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.aws.dto.GetPresignedUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;


    public GetPresignedUrlResponse getPreSignedUrl(String bucketObject, String imageExtension, String userNickname) {

        // 1) 고유성 보장위해 UUID를 사용해 이미지 파일키 제작 (주의: 경로도 key값 만들때 같이 지정해야하기 때문에 bucketObject 경로를 여기에 넣음)
        String keyName = bucketObject + "/" + userNickname + "_" + UUID.randomUUID().toString().replace("-", "") + "." + imageExtension;

        // 2) content-type
        String contentType;
        switch(imageExtension.toLowerCase()) {
        case "jpg": case "jpeg": contentType = "image/jpeg"; break;
        case "png":               contentType = "image/png";  break;
        default:                  contentType = "application/octet-stream";
        }

        // 3) PutObjectRequest 준비
        PutObjectRequest objectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(keyName)
            .contentType(contentType)
            .build();

        // 4) 요청객체 생성, 유효시간 10분
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10))
            .putObjectRequest(objectRequest)
            .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        // 5) URL 반환
        String presignedUrl = presignedRequest.url().toString(); // presignedUrl
        String publicUrl = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, keyName); // 업로드 성공 시 접근 가능한 url

        log.info("[FileService] PublicURL is {}", publicUrl);
        log.info("[FileService] PresignedURL is {}", presignedUrl);

        return GetPresignedUrlResponse.builder()
            .publicUrl(publicUrl)
            .presignedUrl(presignedUrl)
            .build();

    }

    // 캐러셀 전용 presigned URL 생성 메서드
    public GetPresignedUrlResponse getCarouselPresignedUrl(String fileName, String imageExtension) {
        return getPreSignedUrl("carousel", imageExtension, "carousel");
    }

    // S3에서 파일 삭제
    public void deleteFile(String publicUrl) {
        try {
            // publicUrl에서 key 추출
            String key = extractKeyFromUrl(publicUrl);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("[FileService] File deleted successfully: {}", key);

        } catch (Exception e) {
            log.error("[FileService] Failed to delete file: {}", publicUrl, e);
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }

    // URL에서 S3 key 추출하는 헬퍼 메서드
    private String extractKeyFromUrl(String publicUrl) {
        // https://bucket-name.s3.region.amazonaws.com/key 형태에서 key 부분 추출
        String baseUrl = String.format("https://%s.s3.%s.amazonaws.com/", bucket, region);
        if (publicUrl.startsWith(baseUrl)) {
            return publicUrl.substring(baseUrl.length());
        }
        throw new IllegalArgumentException("Invalid S3 URL format: " + publicUrl);
    }
}

