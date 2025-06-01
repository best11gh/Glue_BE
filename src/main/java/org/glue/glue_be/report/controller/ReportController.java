package org.glue.glue_be.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.auth.jwt.CustomUserDetails;
import org.glue.glue_be.common.response.BaseResponse;
import org.glue.glue_be.report.dto.request.CreateReportRequest;
import org.glue.glue_be.report.service.ReportService;
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


    @PatchMapping("/{reportId}/handle")
    @Operation(summary = "[관리자] 신고 처리 완료")
    public BaseResponse<Void> handleReport(@PathVariable Long reportId) {
        reportService.markReportHandled(reportId);
        return new BaseResponse<>();
    }

    @DeleteMapping("/{reportId}")
    @Operation(summary = "[관리자] 신고 삭제")
    public BaseResponse<Void> delete(@PathVariable Long reportId) {
        reportService.delete(reportId);
        return new BaseResponse<>();
    }
}
