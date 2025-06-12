package org.glue.glue_be.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.auth.jwt.CustomUserDetails;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.report.dto.request.CreateReportRequest;
import org.glue.glue_be.report.dto.request.ReportHandledRequest;
import org.glue.glue_be.report.dto.response.ReportResponse;
import org.glue.glue_be.report.service.ReportService;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report")
@Tag(name = "Report", description = "신고 API")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "신고 등록")
    public BaseResponse<Void> create(@AuthenticationPrincipal CustomUserDetails auth,
                                     @Valid @RequestBody CreateReportRequest request) {
        reportService.create(auth.getUserId(), request);
        return new BaseResponse<>();
    }

    @GetMapping
    @Operation(summary = "[관리자] 신고 목록 조회")
    @Secured(value = "ROLE_ADMIN")
    public BaseResponse<List<ReportResponse>> getReports(
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer reasonId,
            @RequestParam(required = false) Boolean handled,
            @RequestParam(required = false) String keyword
    ) {
        List<ReportResponse> result = reportService.getReports(cursorId, pageSize, reasonId, handled, keyword);
        return new BaseResponse<>(result);
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "[관리자] 신고 상세 조회")
    @Secured(value = "ROLE_ADMIN")
    public BaseResponse<ReportResponse> getReport(@PathVariable Long reportId) {
        ReportResponse result = reportService.getReport(reportId);
        return new BaseResponse<>(result);
    }

    @PatchMapping("/{reportId}/handle")
    @Operation(summary = "[관리자] 신고 처리 완료")
    @Secured(value = "ROLE_ADMIN")
    public BaseResponse<Void> handleReport(@PathVariable Long reportId,
                                           @Valid @RequestBody ReportHandledRequest request) {
        reportService.handleReport(reportId, request);
        return new BaseResponse<>();
    }

    @DeleteMapping("/{reportId}")
    @Operation(summary = "[관리자] 신고 삭제")
    @Secured(value = "ROLE_ADMIN")
    public BaseResponse<Void> delete(@PathVariable Long reportId) {
        reportService.delete(reportId);
        return new BaseResponse<>();
    }
}
