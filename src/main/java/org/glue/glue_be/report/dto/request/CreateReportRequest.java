package org.glue.glue_be.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record CreateReportRequest(

        @NotNull(message = "피신고자 ID는 필수입니다.")
        Long reportedId,

        @NotNull(message = "신고 사유 ID는 필수입니다.")
        Long reasonId,

        @NotNull(message = "신고 루트는 필수입니다.")
        @Schema(description = "신고 루트 (0: 모임글, 1: 채팅, 2: 방명록)")
        Integer reportRoute,

        @NotBlank(message = "신고 상세 내용은 필수입니다.")
        String detail
) {
}

