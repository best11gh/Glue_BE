package org.glue.glue_be.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleSignInRequestDto(

        @NotBlank(message = "authorization code는 필수 입력값입니다.")
        String authorizationCode,

        @NotBlank(message = "FCM 토큰은 필수 입력값입니다.")
        String fcmToken

) {
}