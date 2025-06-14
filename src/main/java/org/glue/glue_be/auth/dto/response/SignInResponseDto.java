package org.glue.glue_be.auth.dto.response;

import java.util.List;
import lombok.*;
import org.glue.glue_be.report.dto.response.ReportResponse;

@Builder
public record SignInResponseDto(
        String accessToken,
        int acceptedReportCount,
        List<ReportResponse> acceptedReports
) {
}

