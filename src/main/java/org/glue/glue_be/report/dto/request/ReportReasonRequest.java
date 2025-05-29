package org.glue.glue_be.report.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReportReasonRequest(

        @NotBlank(message = "신고 사유는 필수값입니다.")
        String reason
) {
}
