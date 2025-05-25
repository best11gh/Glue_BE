package org.glue.glue_be.auth.dto.request;


import jakarta.validation.constraints.*;


public record KakaoSignInRequestDto (

	@NotEmpty(message = "카카오 발급 토큰값은 필수입니다")
	String kakaoToken,

	@NotBlank(message = "FCM 토큰은 필수 입력값입니다.")
    String fcmToken

) {}
