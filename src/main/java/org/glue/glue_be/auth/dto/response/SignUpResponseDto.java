package org.glue.glue_be.auth.dto.response;

import lombok.Builder;

@Builder
public record SignUpResponseDto(
        String accessToken
) {
}
