package org.glue.glue_be.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AppleSignInRequestDto(
        @NotBlank(message = "Authorization code는 필수 입력값입니다.")
        String authorizationCode) {
}
