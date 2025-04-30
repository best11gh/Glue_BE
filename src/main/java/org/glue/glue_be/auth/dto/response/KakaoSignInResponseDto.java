package org.glue.glue_be.auth.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class KakaoSignInResponseDto {

	String accessToken;

}
