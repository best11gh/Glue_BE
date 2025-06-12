package org.glue.glue_be.auth.dto.response;

import java.util.List;
import lombok.*;
import org.glue.glue_be.report.dto.response.ReportResponse;

@Getter
@Builder
public class AppleSignInResponseDto {

    private String accessToken;

    private int acceptedReportCount;

    private List<ReportResponse> acceptedReports;
}
