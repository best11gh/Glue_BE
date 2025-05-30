package org.glue.glue_be.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.main.dto.request.CarouselDeployVersionRequest;
import org.glue.glue_be.main.dto.request.MainCarouselCreateRequest;
import org.glue.glue_be.main.dto.response.*;
import org.glue.glue_be.main.service.MainService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/main/carousel")
@RequiredArgsConstructor
@Tag(name = "Carousel", description = "캐러셀 관리 API")
public class MainController {

    private final MainService mainService;

    // 조회
    @GetMapping
    @Operation(summary = "메인 캐러셀 조회", description = "버전 정보에 따라 캐러셀 이미지 조회")
    public ResponseEntity<MainCarouselResponse> getCarouselImages(
            @RequestParam(required = false) String version) {
        MainCarouselResponse response = mainService.getCarouselImages(version);
        return ResponseEntity.ok(response);
    }

    // 모든 버전 목록 조회 (관리자용)
    @GetMapping("/versions")
    @Operation(summary = "[관리자] 캐러셀 모든 버전 목록 조회")
    @Secured(value = "ROLE_ADMIN")
    public ResponseEntity<CarouselVersionResponse> getAllVersions() {
        CarouselVersionResponse response = mainService.getAllVersions();
        return ResponseEntity.ok(response);
    }

    // 현재 배포 버전 조회 (관리자용)
    @GetMapping("/deploy-version")
    @Operation(summary = "[관리자] 현재 배포 중인 캐러셀 버전 조회")
    @Secured(value = "ROLE_ADMIN")
    public String getCurrentDeployVersion() {
        return mainService.getCurrentDeployVersion();
    }

    // 배포 버전 설정 (관리자용)
    @PostMapping("/deploy-version")
    @Operation(summary = "[관리자] 캐러셀 배포 버전 설정")
    @Secured(value = "ROLE_ADMIN")
    public ResponseEntity<CarouselDeployVersionResponse> setDeployVersion(
            @RequestBody CarouselDeployVersionRequest request) {
        CarouselDeployVersionResponse response = mainService.setDeployVersion(request);
        return ResponseEntity.ok(response);
    }

    // 등록용 Presigned URL 생성 (관리자용)
    @PostMapping("/presigned-url")
    @Operation(summary = "[관리자] 캐러셀 이미지 등록용 Presigned URL 생성")
    @Secured(value = "ROLE_ADMIN")
    public ResponseEntity<CarouselPresignedUrlResponse> createPresignedUrlForUpload(
            @RequestBody MainCarouselCreateRequest request) {
        CarouselPresignedUrlResponse response = mainService.createPresignedUrlForUpload(request);
        return ResponseEntity.ok(response);
    }

    // 버전별 일괄 삭제 (관리자용)
    @DeleteMapping("/version/{version}")
    @Operation(summary = "[관리자] 캐러셀 이미지 버전별 일괄 삭제")
    @Secured(value = "ROLE_ADMIN")
    public ResponseEntity<CarouselBulkDeleteResponse> deleteCarouselsByVersion(@PathVariable String version) {
        CarouselBulkDeleteResponse response = mainService.deleteCarouselsByVersion(version);
        return ResponseEntity.ok(response);
    }
}