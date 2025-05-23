package org.glue.glue_be.auth.service;


import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.auth.dto.response.KakaoUserInfoResponseDto;
import org.glue.glue_be.auth.response.AuthResponseStatus;
import org.glue.glue_be.common.exception.BaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoService {

    // final 사용시 DI 한번 주입해 값이 할당된 후 불변성 보장하는 키워드
    private final String clientId;
    private final String redirectUri;
    private final String KAUTH_TOKEN_URL_HOST;
    private final String KAUTH_USER_URL_HOST;


    @Autowired // DI로 카카오 로그인 관련 프로퍼티 값들을 가져온다.
    public KakaoService(@Value("${kakao.client_id}") String clientId,
                        @Value("${kakao.redirect_uri}") String redirectUri) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        KAUTH_TOKEN_URL_HOST = "https://kauth.kakao.com";
        KAUTH_USER_URL_HOST = "https://kapi.kakao.com";
    }


    // 1. 토큰으로 사용자 정보를 얻는 함수
    public KakaoUserInfoResponseDto getUserInfo(String accessToken) {
        try {
            KakaoUserInfoResponseDto userInfo = WebClient.create(KAUTH_USER_URL_HOST)
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .path("/v2/user/me")
                            .build(true))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) // 액세스 토큰 인가
                    .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                    .retrieve()
                    .bodyToMono(KakaoUserInfoResponseDto.class)
                    .block();

            return userInfo;

        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 중 오류 발생: " + e);
            throw new BaseException(AuthResponseStatus.SOCIAL_API_REQUEST_FAILED);
        }
    }


}
