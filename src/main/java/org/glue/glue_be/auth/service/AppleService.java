package org.glue.glue_be.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.*;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.*;

import io.jsonwebtoken.*;

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.text.*;
import java.time.*;
import java.util.*;

import org.apache.commons.io.*;

import org.bouncycastle.asn1.pkcs.*;
import org.bouncycastle.openssl.jcajce.*;

import org.glue.glue_be.auth.config.AppleProperties;
import org.glue.glue_be.auth.dto.response.AppleSocialTokenInfoResponseDto;
import org.glue.glue_be.auth.dto.response.AppleUserInfoResponseDto;
import org.glue.glue_be.auth.response.AuthResponseStatus;
import org.glue.glue_be.common.exception.*;

import lombok.extern.slf4j.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.core.io.*;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.*;
import org.springframework.util.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.*;


@Slf4j
@Service
public class AppleService {

    private final AppleProperties appleProperties;

    @Autowired
    public AppleService(AppleProperties appleProperties) {
        this.appleProperties = appleProperties;
    }


    public AppleUserInfoResponseDto getAppleUserProfile(String idToken) {
//        // 1) Apple 서버에서 토큰 교환
//        AppleSocialTokenInfoResponseDto tokenInfo = requestToken(authorizationCode);

        // 2) 토큰 검증
        verifyIdentityToken(idToken);

        // 3) ID 토큰에서 사용자 정보 추출
        return parseUserInfo(idToken);
    }

    public AppleSocialTokenInfoResponseDto requestToken(String code) {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://appleid.apple.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8")
                .build();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", appleProperties.getAud());
        formData.add("client_secret", generateClientSecret());
        formData.add("grant_type", appleProperties.getGrantType());
        formData.add("code", code);

        try {
            return webClient.post()
                    .uri("/auth/token")
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(AppleSocialTokenInfoResponseDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("[애플 로그인 실패 - 응답 본문]: {}", e.getResponseBodyAsString(), e);
            throw new BaseException(AuthResponseStatus.INVALID_AUTHORIZATION_CODE);
        } catch (Exception e) {
            log.error("[애플 로그인 실패 - 예외]: {}", e.getMessage(), e);
            throw new BaseException(AuthResponseStatus.SOCIAL_API_REQUEST_FAILED);
        }
    }


    private void verifyIdentityToken(String idToken) {
        SignedJWT signedJWT = parseToken(idToken);
        JWTClaimsSet claims = extractClaims(signedJWT);

        verifyExpiration(claims.getExpirationTime());
        verifyIssuer(claims.getIssuer());
        verifyAudience(claims.getAudience());
        verifySignature(signedJWT);
    }

    private AppleUserInfoResponseDto parseUserInfo(String idToken) {
        DecodedJWT jwt = JWT.decode(idToken);
        return AppleUserInfoResponseDto.builder()
                .subject(jwt.getClaim("sub").asString())
                .email(jwt.getClaim("email").asString())
                .build();
    }


    private String generateClientSecret() {
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(5);
        return Jwts.builder()
                .setHeaderParam(JwsHeader.KEY_ID, appleProperties.getKey().getId())
                .setIssuer(appleProperties.getTeamId())
                .setAudience(appleProperties.getIss())
                .setSubject(appleProperties.getAud())
                .setExpiration(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant()))
                .setIssuedAt(new Date())
                .signWith(getPrivateKey(), SignatureAlgorithm.ES256)
                .compact();
    }

    // PrivateKey를 PEM 파일에서 읽어와 변환하여 반환
    private PrivateKey getPrivateKey() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

        try {
            ClassPathResource resource = new ClassPathResource(appleProperties.getKey().getPath());
            try (InputStream is = resource.getInputStream()) {
                String keyContent = IOUtils.toString(is, StandardCharsets.UTF_8);
                keyContent = keyContent.replaceAll("-----BEGIN (.*)-----", "")
                        .replaceAll("-----END (.*)-----", "")
                        .replaceAll("\\s", "");
                byte[] privateKeyBytes = Base64.getDecoder().decode(keyContent);
                PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(privateKeyBytes);
                return converter.getPrivateKey(privateKeyInfo);
            }
        } catch (Exception e) {
            log.error("[애플 로그인 실패 - PrivateKey 변환 실패]: {}", e.getMessage(), e);
            throw new BaseException(AuthResponseStatus.FAIL_APPLE_PRIVATE_KEY);
        }
    }
    private SignedJWT parseToken(String idToken) {
        try {
            return SignedJWT.parse(idToken);
        } catch (ParseException e) {
            log.error("[애플 로그인 실패 - ID 토큰 파싱 오류]: {}", e.getMessage(), e);
            throw new BaseException(AuthResponseStatus.INVALID_ID_TOKEN);
        }
    }

    private JWTClaimsSet extractClaims(SignedJWT signedJWT) {
        try {
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            log.error("[애플 로그인 실패 - 토큰 클레임 추출 실패]: {}", e.getMessage(), e);
            throw new BaseException(AuthResponseStatus.INVALID_ID_TOKEN);
        }
    }


    private void verifyExpiration(Date expirationTime) {
        Date currentTime = new Date();
        if (expirationTime == null || expirationTime.before(currentTime)) {
            log.error("[애플 로그인 실패 - 토큰 만료]: {}", expirationTime);
            throw new BaseException(AuthResponseStatus.EXPIRED_ID_TOKEN);
        }
    }


    private void verifyIssuer(String issuer) {
        if (!appleProperties.getIss().equals(issuer)) {
            log.error("[애플 로그인 실패 - Issuer 불일치]: 기대값 {}, 실제 {}", appleProperties.getIss(), issuer);
            throw new BaseException(AuthResponseStatus.INVALID_ID_TOKEN);
        }
    }


    private void verifyAudience(List<String> audience) {
        if (audience == null || audience.isEmpty() || !appleProperties.getAud().equals(audience.get(0))) {
            String actual = (audience != null && !audience.isEmpty()) ? audience.get(0) : "null";
            log.error("[애플 로그인 실패 - Audience 불일치]: 기대값 {}, 실제 {}", appleProperties.getAud(), actual);
            throw new BaseException(AuthResponseStatus.INVALID_ID_TOKEN);
        }
    }

    private void verifySignature(SignedJWT signedJWT) {
        String keyId = signedJWT.getHeader().getKeyID();
        try {
            URL publicKeyUrl = new URL(appleProperties.getAuth().getPublicKeyUrl());
            JWKSet jwkSet = JWKSet.load(publicKeyUrl);
            JWK jwk = jwkSet.getKeyByKeyId(keyId);

            if (jwk == null) {
                log.error("[애플 로그인 실패 - 키 ID 없음]: {}", keyId);
                throw new BaseException(AuthResponseStatus.INVALID_ID_TOKEN);
            }

            RSAPublicKey publicKey = ((com.nimbusds.jose.jwk.RSAKey) jwk).toRSAPublicKey();
            JWSVerifier verifier = new RSASSAVerifier(publicKey);

            if (!signedJWT.verify(verifier)) {
                log.error("[애플 로그인 실패 - 서명 검증 실패]");
                throw new BaseException(AuthResponseStatus.SIGNATURE_VERIFICATION_FAILED);
            }
        } catch (Exception e) {
            log.error("[애플 로그인 실패 - 서명 검증 중 예외 발생]: {}", e.getMessage(), e);
            throw new BaseException(AuthResponseStatus.SIGNATURE_VERIFICATION_FAILED);
        }
    }

}
