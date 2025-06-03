package org.glue.glue_be.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AppleSignInRequestDto(
        @NotBlank(message = "id token은 필수 입력값입니다.")
        String idToken,

        @NotBlank(message = "FCM 토큰은 필수 입력값입니다.")
        String fcmToken) {
}
