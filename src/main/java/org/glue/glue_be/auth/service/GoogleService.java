package org.glue.glue_be.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.auth.config.GoogleProperties;
import org.glue.glue_be.auth.dto.response.GoogleTokenInfoResponseDto;
import org.glue.glue_be.auth.dto.response.GoogleUserInfoResponseDto;
import org.glue.glue_be.auth.response.AuthResponseStatus;
import org.glue.glue_be.common.exception.BaseException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleService {

    private final GoogleProperties googleProperties;


    public GoogleUserInfoResponseDto getGoogleUserInfo(String code) {
        // 1) Google 서버에서 토큰 받기
        GoogleTokenInfoResponseDto tokenInfo = requestToken(code);

        // 2) token으로 사용자 정보 추출
        return requestUserInfo(tokenInfo.accessToken());
    }


    private GoogleTokenInfoResponseDto requestToken(String code) {
        WebClient webClient = WebClient.builder()
                .baseUrl(googleProperties.getTokenUri())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8")
                .build();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("client_id", googleProperties.getClientId());
        formData.add("client_secret", googleProperties.getClientSecret());
        formData.add("redirect_uri", googleProperties.getRedirectUri());
        formData.add("grant_type", "authorization_code");

        try {
            return webClient.post()
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(GoogleTokenInfoResponseDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("[구글 로그인 실패 - 응답 본문]: {}", e.getResponseBodyAsString(), e);
            throw new BaseException(AuthResponseStatus.INVALID_AUTHORIZATION_CODE, "Google 인증 코드가 유효하지 않습니다.");
        } catch (Exception e) {
            log.error("[구글 로그인 실패 - 예외]: {}", e.getMessage(), e);
            throw new BaseException(AuthResponseStatus.SOCIAL_API_REQUEST_FAILED);
        }
    }

    private GoogleUserInfoResponseDto requestUserInfo(String accessToken) {
        WebClient webClient = WebClient.builder()
                .baseUrl(googleProperties.getResourceUri())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .build();

        try {
            return webClient.get()
                    .retrieve()
                    .bodyToMono(GoogleUserInfoResponseDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("[구글 유저 정보 조회 실패 - 응답 본문]: {}", e.getResponseBodyAsString(), e);
            throw new BaseException(AuthResponseStatus.INVALID_ACCESS_TOKEN, "Google Access Token이 유효하지 않습니다.");
        } catch (Exception e) {
            log.error("[구글 유저 정보 조회 실패 - 예외]: {}", e.getMessage(), e);
            throw new BaseException(AuthResponseStatus.SOCIAL_API_REQUEST_FAILED);
        }
    }
}
