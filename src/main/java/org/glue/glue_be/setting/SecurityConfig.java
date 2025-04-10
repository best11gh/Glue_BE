package org.glue.glue_be.setting;


import lombok.RequiredArgsConstructor;
import org.glue.glue_be.common.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@RequiredArgsConstructor // for DI fields
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CustomJwtAuthenticationEntryPoint customJwtAuthenticationEntryPoint;
	private final CustomAccessDeniedHandler customAccessDeniedHandler;


	// 인증과정 배제 경로 -> 앞으로추가해나가야함
	private static final String[] AUTH_WHITELIST = {
		"/api/auth/kakao/signup"
	};

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(form -> form.disable()) // 폼 로그인 비활성화
			.sessionManagement(session -> {
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
			})
			.exceptionHandling(exception -> {
				exception.authenticationEntryPoint(customJwtAuthenticationEntryPoint);
				exception.accessDeniedHandler(customAccessDeniedHandler);
			})
		;

		http.authorizeHttpRequests(auth -> {
			auth.requestMatchers(AUTH_WHITELIST).permitAll();
//			auth.anyRequest().authenticated();
			auth.anyRequest().permitAll(); // 개발단계에선 일단 모든 경로에 허용, 원랜 화이트리스트의 경로만 적용해야함
			})
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


		return http.build();
	}
}
