package org.glue.glue_be.user.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter // Jackson이 직렬화(dto -> json)할 때 getter을 필요로함
@Builder
public class KakaoSignUpResponseDto {

	String accessToken;

}
