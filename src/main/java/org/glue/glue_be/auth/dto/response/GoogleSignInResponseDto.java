package org.glue.glue_be.auth.dto.response;

import lombok.Builder;
import org.glue.glue_be.report.dto.response.ReportResponse;

import java.util.List;

@Builder
public record GoogleSignInResponseDto(
        String accessToken,
        int acceptedReportCount,
        List<ReportResponse> acceptedReports
) {
}
