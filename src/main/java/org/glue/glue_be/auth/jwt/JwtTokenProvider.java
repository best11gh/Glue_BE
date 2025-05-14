package org.glue.glue_be.auth.jwt;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;


// Jwt Token 만드는 클래스
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

	private static final String MEMBER_ID = "memberId";
	private static final String MEMBER_NICKNAME = "memberNickname";
	private static final Long TOKEN_EXPIRATION_TIME = 24 * 60 * 60 * 1000L; // todo: 토큰 유효기간 논의, 현재는 24시간


	@Value("${jwt.secret}")
	private String JWT_SECRET;


	// 앱 실행 직전 jwt secret 키를 주입받은 후 base64로 인코딩하는 작업
	// -> jjwt 라이브러리는 토큰이 base64 인코딩 문자열로 받아야해서 하는 작업
	@PostConstruct // 해당 빈이 주입된 후 추가적인 초기화 작업할때 붙이는 어노테이션
	protected void init() {
		// base64 라이브러리에서 encodeToString을 이용해 byte[] -> String 타입으로 변환
		JWT_SECRET = Base64.getEncoder().encodeToString(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
	}

	public String generateToken(Authentication authentication) {

		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		final Date now = new Date();

		// 1. 클레임 생성. jwt의 claims엔 여러개가 있는데 우선 토큰 발급시각과 토큰 만료시간 지정
		final Claims claims = Jwts.claims()
			.setIssuedAt(now)
			.setExpiration(new Date(now.getTime() + TOKEN_EXPIRATION_TIME));

		// 2. userId와 nickname을 JWT 클레임에 삽입
		claims.put(MEMBER_ID, userDetails.getUserId());
		claims.put(MEMBER_NICKNAME, userDetails.getUserNickname());

		// 3. header, 방금 조립한 claim, 그리고 signature을 합쳐 JWT 빌드
		return Jwts.builder()
			.setHeaderParam(Header.TYPE, Header.JWT_TYPE)
			.setClaims(claims)
			.signWith(getSigningKey())
			.compact();
	}


	// 서명 생성
	private Key getSigningKey() {
		// 1. jwt 비밀 키를 인코드 한 값을 가져옴
		String encodedKey = Base64.getEncoder().encodeToString(JWT_SECRET.getBytes());

		// 2. 주어진 키 값을 기반으로 HMAC-SHA 암호화 알고리즘에 쓰이는 Key 객체 리턴
		return Keys.hmacShaKeyFor(encodedKey.getBytes());
	}

	public JwtValidationType validateToken(String token) {
		try {
			final Claims claims = getBody(token);
			return JwtValidationType.VALID_JWT;
		} catch (MalformedJwtException ex) {
			return JwtValidationType.INVALID_JWT_TOKEN;
		} catch (ExpiredJwtException ex) {
			return JwtValidationType.EXPIRED_JWT_TOKEN;
		} catch (UnsupportedJwtException ex) {
			return JwtValidationType.UNSUPPORTED_JWT_TOKEN;
		} catch (IllegalArgumentException ex) {
			return JwtValidationType.EMPTY_JWT;
		}
	}


	// claim 파싱 함수
	private Claims getBody(final String token) {
		return Jwts.parserBuilder()
			.setSigningKey(getSigningKey())
			.build()
			.parseClaimsJws(token)
			.getBody();
	}

	// claim에서 유저 인증주체 정보 파싱 메서드
	public Long getUserIdFromJwt(String token) {
		Claims claims = getBody(token);
		return Long.valueOf(claims.get(MEMBER_ID).toString());
	}

	public String getUserNicknameFromJwt(String token) {
		Claims claims = getBody(token);
		return claims.get(MEMBER_NICKNAME).toString();
	}

	public Authentication getAuthentication(String token) {
		Long userId = getUserIdFromJwt(token);
		String userNickname = getUserNicknameFromJwt(token);

		// 사용자 정보를 기반으로 UserDetails 객체 생성
		CustomUserDetails userDetails = new CustomUserDetails(userId, userNickname);

		// 인증 객체 생성 및 반환
		return new UsernamePasswordAuthenticationToken(
				userDetails, "", Collections.emptyList());
	}
}
