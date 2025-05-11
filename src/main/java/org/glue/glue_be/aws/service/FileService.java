package org.glue.glue_be.aws.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.aws.dto.GetPresignedUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;


    public GetPresignedUrlResponse getPreSignedUrl(String bucketObject, String imageExtension) {

        // 1) 고유성 보장위해 UUID를 사용해 이미지 파일키 제작 (주의: 경로도 key값 만들때 같이 지정해야하기 때문에 bucketObject 경로를 여기에 넣음)
        String keyName = bucketObject + "/" + UUID.randomUUID().toString().replace("-", "") + "." + imageExtension;

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
}

