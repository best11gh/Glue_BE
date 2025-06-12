package org.glue.glue_be.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record ReportHandledRequest(
        @NotNull(message = "신고 처리 결과는 필수입니다.")
        @Schema(description = "true: 수락 / false: 거절")
        boolean accept
) {
}
