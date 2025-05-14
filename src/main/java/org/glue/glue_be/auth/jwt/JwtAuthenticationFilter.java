package org.glue.glue_be.auth.jwt;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.glue.glue_be.auth.jwt.JwtValidationType.VALID_JWT;



// 클라이언트가 보내는 HTTP 요청의 JWT 토큰을 확인하고 토큰이 유효할 경우 Spring Security의 인증 객체를 설정해주는 필터단
// 한 Http 요청에 한번씩만 실행되는 OncePerRequestFilter 상속받음

@Component
@RequiredArgsConstructor // for DI fields
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	// Jwt 생성 제공자 DI
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(
		@NonNull HttpServletRequest request,
		@NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain) throws ServletException, IOException {
		try {
			// 1. 요청에서 token 값을 파싱
			final String token = getJwtFromRequest(request);

			// 2. 만약 토큰이 유효한 토큰이라면
			if (jwtTokenProvider.validateToken(token) == VALID_JWT) {

				// 2-1. 토큰에서 userId, userNickname 추출
				Long userId = jwtTokenProvider.getUserIdFromJwt(token);
				String userNickname = jwtTokenProvider.getUserNicknameFromJwt(token);

				// 2-2. 유저정보를 담는 객체를 만들기 위해 userDetails 인터페이스에 따른 커스텀 객체를 초기화합니다.
				CustomUserDetails userDetails = new CustomUserDetails(userId, userNickname);

				// 유저 핵심정보를 넣는 principal은 추후 컨트롤러의 @AuthenticationPrincipal로 받게되는 타입이 됩니다.
				// 따라서 우리가 컨트롤러에서 해당 어노테이션을 붙인 값의 타입은 CustomUserDetails가 돼야합니다!
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
					userDetails, null, null
				);
				auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				// 2-3. 유저 정보를 담은 auth 객체를 이후 필터, 컨트롤러에서 인식하게 spring security context에 넣는다.
				SecurityContextHolder.getContext().setAuthentication(auth);

			}
		} catch (Exception exception) {
			throw new RuntimeException("JWT 인증 처리 중 오류 발생", exception);
		}

		// 3. 다음 필터로 요청 전달
		filterChain.doFilter(request, response);
	}



	private String getJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");

		// Request의 Authorizaion 헤더값이 Bearer 123124152일 때 토큰값인 123124152만 파싱하여 리턴
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring("Bearer ".length());
		}
		return null;
	}
}