package org.glue.glue_be.user.service;

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

import org.glue.glue_be.common.config.*;
import org.glue.glue_be.common.exception.*;
import org.glue.glue_be.common.response.*;
import org.glue.glue_be.user.dto.response.*;

import lombok.extern.slf4j.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.stereotype.*;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;


@Slf4j
@Service
public class AppleService {

//    private static final String NONCE = "20B20D-0S8-1K8"; // TODO: 프론트 쪽과 협의 필요 (지금은 임시 값)

    private final AppleProperties appleProperties;
    private final SocialConfig socialConfig;

    @Autowired
    public AppleService(AppleProperties appleProperties, SocialConfig socialConfig) {
        this.appleProperties = appleProperties;
        this.socialConfig = socialConfig;
    }


    public AppleUserInfoResponseDto getAppleUserProfile(String authorizationCode) {
        // 1) Apple 서버에서 토큰 교환
        AppleSocialTokenInfoResponseDto tokenInfo = requestToken(authorizationCode);

        // 2) 토큰 검증
        verifyIdentityToken(tokenInfo.idToken());

        // 3) ID 토큰에서 사용자 정보 추출
        return parseUserInfo(tokenInfo.idToken());
    }

    private AppleSocialTokenInfoResponseDto requestToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(APPLICATION_FORM_URLENCODED_VALUE));

        String requestBody = "client_id=" + appleProperties.getAud()
                + "&client_secret=" + generateClientSecret()
                + "&grant_type=" + appleProperties.getGrantType()
                + "&code=" + code;

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<AppleSocialTokenInfoResponseDto> response = socialConfig.restTemplate().exchange(
                appleProperties.getAuth().getTokenUrl(),
                HttpMethod.POST,
                request,
                AppleSocialTokenInfoResponseDto.class);

        return response.getBody();
    }

    public void verifyIdentityToken(String idToken) {
        SignedJWT signedJWT = parseToken(idToken);
        JWTClaimsSet claims = extractClaims(signedJWT);

        verifyExpiration(claims.getExpirationTime());
//        verifyNonce(claims.getStringClaim("nonce"));
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
            throw new RuntimeException("PrivateKey 변환 실패", e);
        }
    }

    private SignedJWT parseToken(String idToken) {
        try {
            return SignedJWT.parse(idToken);
        } catch (ParseException e) {
            throw new BaseException(BaseResponseStatus.WRONG_JWT_TOKEN,
                    "토큰 디코딩 실패: " + idToken + " - " + e.getMessage());
        }
    }


    private JWTClaimsSet extractClaims(SignedJWT signedJWT) {
        try {
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new BaseException(BaseResponseStatus.WRONG_JWT_TOKEN,
                    "토큰 클레임 추출 실패: " + e.getMessage());
        }
    }


    private void verifyExpiration(Date expirationTime) {
        Date currentTime = new Date();
        if (expirationTime == null || expirationTime.before(currentTime)) {
            throw new BaseException(BaseResponseStatus.WRONG_JWT_TOKEN,
                    "토큰 만료됨: 만료 시간은 " + expirationTime + "입니다.");
        }
    }

    // TODO: 프론트 쪽과 협의 필요
//    private void verifyNonce(String nonce) {
//        if (nonce == null || !NONCE.equals(nonce)) {
//            throw new BaseException(BaseResponseStatus.WRONG_JWT_TOKEN,
//                    "Nonce 불일치: 기대값 " + NONCE + ", 실제 " + nonce);
//        }
//    }


    private void verifyIssuer(String issuer) {
        if (!appleProperties.getIss().equals(issuer)) {
            throw new BaseException(BaseResponseStatus.WRONG_JWT_TOKEN,
                    "Issuer 불일치: 기대값 " + appleProperties.getIss() + ", 실제 " + issuer);
        }
    }


    private void verifyAudience(List<String> audience) {
        if (audience == null || audience.isEmpty() || !appleProperties.getAud().equals(audience.get(0))) {
            String actual = (audience != null && !audience.isEmpty()) ? audience.get(0) : "null";
            throw new BaseException(BaseResponseStatus.WRONG_JWT_TOKEN,
                    "Audience 불일치: 기대값 " + appleProperties.getAud() + ", 실제 " + actual);
        }
    }

    private void verifySignature(SignedJWT signedJWT) {
        String keyId = signedJWT.getHeader().getKeyID();
        try {
            URL publicKeyUrl = new URL(appleProperties.getAuth().getPublicKeyUrl());
            JWKSet jwkSet = JWKSet.load(publicKeyUrl);
            JWK jwk = jwkSet.getKeyByKeyId(keyId);

            if (jwk == null) {
                throw new BaseException(BaseResponseStatus.WRONG_JWT_TOKEN, "키 아이디를 찾을 수 없음: " + keyId);
            }

            RSAPublicKey publicKey = ((com.nimbusds.jose.jwk.RSAKey) jwk).toRSAPublicKey();
            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            if (!signedJWT.verify(verifier)) {
                throw new BaseException(BaseResponseStatus.WRONG_JWT_TOKEN, "서명 검증 실패");
            }
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.WRONG_JWT_TOKEN,
                    "서명 검증 중 오류 발생: " + e.getMessage());
        }
    }
}
