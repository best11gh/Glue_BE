package org.glue.glue_be.report.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateReportRequest(

        @NotNull(message = "피신고자 ID는 필수입니다.")
        Long reportedId,

        @NotNull(message = "신고 사유 ID는 필수입니다.")
        Long reasonId

) {
}

