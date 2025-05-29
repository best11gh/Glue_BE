package org.glue.glue_be.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.report.dto.request.ReportReasonRequest;
import org.glue.glue_be.report.dto.response.ReportReasonResponse;
import org.glue.glue_be.report.service.ReportReasonService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report-reason")
@Tag(name = "Report Reason", description = "신고 사유 API")
public class ReportReasonController {

    private final ReportReasonService reportReasonService;

    // TODO: [관리자] => 어드민만 접근 가능하게 하기

    @PostMapping
    @Operation(summary = "[관리자] 신고 사유 추가")
    public BaseResponse<ReportReasonResponse> create(@Valid @RequestBody ReportReasonRequest request) {
        ReportReasonResponse reason = reportReasonService.create(request);
        return new BaseResponse<>(reason);
    }

    @PutMapping("/{reportReasonId}")
    @Operation(summary = "[관리자] 신고 사유 수정")
    public BaseResponse<ReportReasonResponse> update(@PathVariable Long reportReasonId,
                                                     @Valid @RequestBody ReportReasonRequest request) {
        ReportReasonResponse reason = reportReasonService.update(reportReasonId, request);
        return new BaseResponse<>(reason);
    }

    @GetMapping
    @Operation(summary = "신고 사유 전체 조회")
    public BaseResponse<ReportReasonResponse[]> getReportReasons() {
        ReportReasonResponse[] reasons = reportReasonService.getReasons();
        return new BaseResponse<>(reasons);
    }


    @DeleteMapping("/{reportReasonId}")
    @Operation(summary = "[관리자] 신고 사유 삭제")
    public BaseResponse<Void> delete(@PathVariable Long reportReasonId) {
        reportReasonService.delete(reportReasonId);
        return new BaseResponse<>();
    }
}
