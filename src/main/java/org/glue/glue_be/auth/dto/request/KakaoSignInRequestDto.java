package org.glue.glue_be.auth.dto.request;


import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;


@Getter
public class KakaoSignInRequestDto {

	@NotEmpty(message = "카카오 발급 토큰값은 필수입니다")
	String kakaoToken;

}
