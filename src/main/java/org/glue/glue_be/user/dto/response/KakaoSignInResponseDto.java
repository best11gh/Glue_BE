package org.glue.glue_be.user.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class KakaoSignInResponseDto {

	String accessToken;

}
