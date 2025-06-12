package org.glue.glue_be.report.dto.response;

import lombok.Builder;
import org.glue.glue_be.report.entity.Report;

import java.time.LocalDateTime;

@Builder
public record ReportResponse(
        Long reportId,
        String reportedNickname,
        String reportedEmail,
        String reason,
        String reportRoute,
        String detail,
        Boolean accepted,
        LocalDateTime createdAt
) {
    public static ReportResponse from(Report report) {
        return ReportResponse.builder()
                .reportId(report.getReportId())
                .reportedNickname(report.getReported().getNickname())
                .reportedEmail(report.getReported().getEmail())
                .reason(report.getReason().getReason())
                .reportRoute(convertRoute(report.getReportRoute()))
                .detail(report.getDetail())
                .accepted(report.getAccepted())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private static String convertRoute(Integer route) {
        return switch (route) {
            case 0 -> "모임글";
            case 1 -> "채팅";
            case 2 -> "방명록";
            default -> "기타";
        };
    }
}