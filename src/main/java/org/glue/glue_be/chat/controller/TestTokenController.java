package org.glue.glue_be.chat.controller;

import org.glue.glue_be.auth.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
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
}