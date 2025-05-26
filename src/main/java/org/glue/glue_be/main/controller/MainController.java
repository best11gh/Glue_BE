package org.glue.glue_be.main.controller;

import lombok.RequiredArgsConstructor;
import org.glue.glue_be.main.dto.request.CarouselDeployVersionRequest;
import org.glue.glue_be.main.dto.request.MainCarouselCreateRequest;
import org.glue.glue_be.main.dto.response.*;
import org.glue.glue_be.main.service.MainService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/main/carousel")
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    // 조회
    @GetMapping
    public ResponseEntity<MainCarouselResponse> getCarouselImages(
            @RequestParam(required = false) String version) {
        MainCarouselResponse response = mainService.getCarouselImages(version);
        return ResponseEntity.ok(response);
    }

    // TODO: 관리자용 api들 관리자만 접근 가능하도록
    // 모든 버전 목록 조회 (관리자용)
    @GetMapping("/versions")
    public ResponseEntity<CarouselVersionResponse> getAllVersions() {
        CarouselVersionResponse response = mainService.getAllVersions();
        return ResponseEntity.ok(response);
    }

    // 현재 배포 버전 조회 (관리자용)
    @GetMapping("/deploy-version")
    public String getCurrentDeployVersion() {
        return mainService.getCurrentDeployVersion();
    }

    // 배포 버전 설정 (관리자용)
    @PostMapping("/deploy-version")
    public ResponseEntity<CarouselDeployVersionResponse> setDeployVersion(
            @RequestBody CarouselDeployVersionRequest request) {
        CarouselDeployVersionResponse response = mainService.setDeployVersion(request);
        return ResponseEntity.ok(response);
    }

    // 등록용 Presigned URL 생성 (관리자용)
    @PostMapping("/presigned-url")
    public ResponseEntity<CarouselPresignedUrlResponse> createPresignedUrlForUpload(
            @RequestBody MainCarouselCreateRequest request) {
        CarouselPresignedUrlResponse response = mainService.createPresignedUrlForUpload(request);
        return ResponseEntity.ok(response);
    }

    // 버전별 일괄 삭제 (관리자용)
    @DeleteMapping("/version/{version}")
    public ResponseEntity<CarouselBulkDeleteResponse> deleteCarouselsByVersion(@PathVariable String version) {
        CarouselBulkDeleteResponse response = mainService.deleteCarouselsByVersion(version);
        return ResponseEntity.ok(response);
    }
}