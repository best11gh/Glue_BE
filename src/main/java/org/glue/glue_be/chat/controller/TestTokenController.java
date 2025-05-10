package org.glue.glue_be.chat.controller;

import org.glue.glue_be.auth.jwt.JwtTokenProvider;
import org.glue.glue_be.auth.jwt.JwtValidationType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TestTokenController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/api/test/generate-tokens")
    public String generateTestTokens() {
        UUID userUuid1 = UUID.fromString("b2d6f7c9-a842-41e5-b390-63487e10d9fc");
        UUID userUuid3 = UUID.fromString("d4f8f9e1-c064-53a7-d5b2-85609a32f1be");

        // 인증 객체 생성
        Authentication auth1 = new UsernamePasswordAuthenticationToken(userUuid1.toString(), null);
        Authentication auth3 = new UsernamePasswordAuthenticationToken(userUuid3.toString(), null);

        // 토큰 생성
        String token1 = jwtTokenProvider.generateToken(auth1);
        String token2 = jwtTokenProvider.generateToken(auth3);

        return "사용자 1번 토큰: " + token1 + "\n\n사용자 3번 토큰: " + token2;
    }

    @GetMapping("/api/test/parse-tokens")
    public ResponseEntity<String> parseHardcodedTokens() {
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpYXQiOjE3NDY4NzcwODIsImV4cCI6MTc0Njk2MzQ4MiwibWVtYmVySWQiOiJhZDFmYTkyMy01NzdhLTQ0MzYtYWM2NS1mYjYzNzZiNDk0ZjIifQ.5IsDw4dGviy51WQkA0m_ztg_N10A4YOV_3rhF_elmDNP1XmzWv_0xj7OjvgotZT0KLGNENxPdY8uG4pqimUiyA";
        String results = "";

        try {
            JwtValidationType validationType1 = jwtTokenProvider.validateToken(token);
            if (validationType1 == JwtValidationType.VALID_JWT) {
                Authentication auth1 = jwtTokenProvider.getAuthentication(token);
                String uuid1 = auth1.getName();
                results = uuid1;
            }

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
        }
    }
}