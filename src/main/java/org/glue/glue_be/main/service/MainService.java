package org.glue.glue_be.main.service;

import lombok.RequiredArgsConstructor;
import org.glue.glue_be.aws.service.FileService;
import org.glue.glue_be.aws.dto.GetPresignedUrlResponse;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.main.dto.*;
import org.glue.glue_be.main.dto.request.CarouselDeployVersionRequest;
import org.glue.glue_be.main.dto.request.MainCarouselCreateRequest;
import org.glue.glue_be.main.dto.response.*;
import org.glue.glue_be.main.entity.MainCarousel;
import org.glue.glue_be.main.entity.CarouselDeployVersion;
import org.glue.glue_be.main.repository.MainCarouselRepository;
import org.glue.glue_be.main.repository.CarouselDeployVersionRepository;
import org.glue.glue_be.main.response.MainResponseStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MainService {

    private final MainCarouselRepository mainCarouselRepository;
    private final CarouselDeployVersionRepository deployVersionRepository;
    private final FileService fileService;

    // 조회 (배포 버전 기준)
    @Transactional(readOnly = true)
    public MainCarouselResponse getCarouselImages(String version) {
        // admin 페이지가 아니라 프론트에서 이 서비스를 호출할 땐 version 파라미터를 주지 않을 것
        // 그러므로 version이 파라미터로 들어오지 않을 땐 현재 배포되어 있는 버전을 조회
        if (version == null || version.trim().isEmpty()) {
            version = getCurrentDeployVersion();
        }

        List<MainCarousel> carousels = getCarouselsByVersion(version);

        List<CarouselImageDto> images = carousels.stream()
                .map(CarouselImageDto::fromEntity)
                .collect(Collectors.toList());

        return new MainCarouselResponse(version, images, images.size());
    }

    // 현재 배포 버전 조회
    @Transactional(readOnly = true)
    public String getCurrentDeployVersion() {
        return getActiveDeployVersion()
                .map(CarouselDeployVersion::getVersion)
                .orElseThrow(() -> new BaseException(MainResponseStatus.NO_ACTIVE_DEPLOY_VERSION));
    }

    // 배포 버전 설정
    @Transactional
    public CarouselDeployVersionResponse setDeployVersion(CarouselDeployVersionRequest request) {
        validateDeployVersionRequest(request);

        // 해당 버전의 캐러셀이 존재하는지 확인
        List<MainCarousel> carousels = getCarouselsByVersion(request.version());
        if (carousels.isEmpty()) {
            throw new BaseException(MainResponseStatus.NO_CAROUSELS_FOR_VERSION);
        }

        // 기존 활성 버전 비활성화
        getActiveDeployVersion().ifPresent(CarouselDeployVersion::deactivate);

        // 새 버전을 활성화하거나 생성
        CarouselDeployVersion deployVersion = getDeployVersionByVersion(request.version())
                .orElse(request.toEntity());

        if (deployVersion.getId() != null) {
            // 기존 버전 업데이트
            deployVersion.updateVersion(request.version(), request.description());
            deployVersion.activate();
        }

        CarouselDeployVersion saved = deployVersionRepository.save(deployVersion);

        return CarouselDeployVersionResponse.fromEntity(saved);
    }

    // 모든 버전 목록 조회
    @Transactional(readOnly = true)
    public CarouselVersionResponse getAllVersions() {
        List<String> versions = getAllCarouselVersions();
        return new CarouselVersionResponse(versions, versions.size());
    }

    // 등록용 Presigned URL 생성
    @Transactional
    public CarouselPresignedUrlResponse createPresignedUrlForUpload(MainCarouselCreateRequest request) {
        validateCreateRequest(request);

        // displayOrder 자동 설정 (현재 버전의 최대값 + 1)
        Integer maxOrder = getMaxDisplayOrderByVersion(request.version());
        Integer displayOrder = (maxOrder != null ? maxOrder : 0) + 1;

        // 파일 확장자 추출
        String imageExtension = extractFileExtension(request.fileName());

        // 기존 FileService 활용
        GetPresignedUrlResponse s3Response = fileService.getCarouselPresignedUrl(request.fileName(), imageExtension);

        // DB에 저장
        MainCarousel carousel = request.toEntity(s3Response.getPublicUrl(), displayOrder);

        MainCarousel savedCarousel = mainCarouselRepository.save(carousel);

        return CarouselPresignedUrlResponse.fromEntity(s3Response, request.fileName(), savedCarousel.getId());
    }

    // 버전별 일괄 삭제
    @Transactional
    public CarouselBulkDeleteResponse deleteCarouselsByVersion(String version) {
        if (version == null || version.trim().isEmpty()) {
            throw new BaseException(MainResponseStatus.VERSION_REQUIRED);
        }

        // 해당 버전의 모든 캐러셀 조회
        List<MainCarousel> carousels = getCarouselsByVersion(version);

        if (carousels.isEmpty()) {
            return new CarouselBulkDeleteResponse(version, 0, 0, 0);
        }

        int totalCount = carousels.size();
        int successCount = 0;
        int failedCount = 0;

        // 각 캐러셀 삭제 처리
        for (MainCarousel carousel : carousels) {
            try {
                // S3에서 이미지 삭제 시도
                if (carousel.getImageUrl() != null) {
                    try {
                        fileService.deleteFile(carousel.getImageUrl());
                    } catch (Exception e) {
                        System.err.println("Failed to delete S3 file for carousel ID " + carousel.getId() + ": " + e.getMessage());
                        // S3 삭제 실패해도 DB 삭제는 계속 진행
                    }
                }

                // DB에서 삭제
                mainCarouselRepository.delete(carousel);
                successCount++;

            } catch (Exception e) {
                System.err.println("Failed to delete carousel ID " + carousel.getId() + ": " + e.getMessage());
                failedCount++;
            }
        }

        return new CarouselBulkDeleteResponse(version, totalCount, successCount, failedCount);
    }

    private List<MainCarousel> getCarouselsByVersion(String version) {
        return mainCarouselRepository.findByVersionOrderByDisplayOrder(version);
    }

    private Optional<CarouselDeployVersion> getActiveDeployVersion() {
        return deployVersionRepository.findByIsActiveTrue();
    }

    private Optional<CarouselDeployVersion> getDeployVersionByVersion(String version) {
        return deployVersionRepository.findByVersion(version);
    }

    private List<String> getAllCarouselVersions() {
        return mainCarouselRepository.findAllVersions();
    }

    private Integer getMaxDisplayOrderByVersion(String version) {
        return mainCarouselRepository.findMaxDisplayOrderByVersion(version);
    }

    private void validateDeployVersionRequest(CarouselDeployVersionRequest request) {
        if (request.version() == null || request.version().trim().isEmpty()) {
            throw new BaseException(MainResponseStatus.VERSION_REQUIRED);
        }
    }

    private String extractFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new BaseException(MainResponseStatus.INVALID_FILE_NAME);
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private void validateCreateRequest(MainCarouselCreateRequest request) {
        if (request.version() == null || request.version().trim().isEmpty()) {
            throw new BaseException(MainResponseStatus.VERSION_REQUIRED);
        }
        if (request.fileName() == null || request.fileName().trim().isEmpty()) {
            throw new BaseException(MainResponseStatus.FILE_NAME_REQUIRED);
        }
    }
}