package org.glue.glue_be.auth.dto.request;


import lombok.Getter;


@Getter
public class KakaoSignInRequestDto {

    String kakaoToken;
    String fcmToken;

}
