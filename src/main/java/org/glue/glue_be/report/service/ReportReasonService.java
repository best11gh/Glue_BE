package org.glue.glue_be.report.service;


import lombok.RequiredArgsConstructor;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.report.dto.request.ReportReasonRequest;
import org.glue.glue_be.report.dto.response.ReportReasonResponse;
import org.glue.glue_be.report.entity.ReportReason;
import org.glue.glue_be.report.repository.ReportReasonRepository;
import org.glue.glue_be.report.response.ReportResponseStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportReasonService {

    private final ReportReasonRepository reportReasonRepository;

    // 신고 사유 추가
    public ReportReasonResponse create(ReportReasonRequest request) {
        validateDuplicateReason(request.reason());

        ReportReason reason = ReportReason.builder()
                .reason(request.reason())
                .build();

        ReportReason saved = reportReasonRepository.save(reason);
        return toResponse(saved);
    }

    // 신고 사유 삭제
    public void delete(Long id) {
        ReportReason reason = findReason(id);
        reportReasonRepository.delete(reason);
    }

    // 신고 사유 수정
    public ReportReasonResponse update(Long id, ReportReasonRequest request) {
        validateDuplicateReason(request.reason());

        ReportReason reason = findReason(id);
        reason.update(request.reason());

        return toResponse(reason);
    }


    // 신고 사유 전체 조회
    @Transactional(readOnly = true)
    public ReportReasonResponse[] getReasons() {
        return reportReasonRepository.findAll().stream()
                .map(this::toResponse)
                .toArray(ReportReasonResponse[]::new);
    }

    // 같은 신고 사유 존재 여부
    private void validateDuplicateReason(String reason) {
        if (reportReasonRepository.existsByReason(reason)) {
            throw new BaseException(ReportResponseStatus.DUPLICATE_REPORT_REASON);
        }
    }

    private ReportReasonResponse toResponse(ReportReason reason) {
        return new ReportReasonResponse(reason.getReportReasonId(), reason.getReason());
    }

    private ReportReason findReason(Long reasonId) {
        return reportReasonRepository.findById(reasonId)
                .orElseThrow(() -> new BaseException(ReportResponseStatus.REPORT_REASON_NOT_FOUND));
    }

}
