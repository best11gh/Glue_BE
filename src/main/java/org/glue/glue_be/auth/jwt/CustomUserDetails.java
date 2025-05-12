package org.glue.glue_be.auth.jwt;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;


// Principal에 들어갈 유저 정보를 담고있는 객체
public class CustomUserDetails implements UserDetails {

	// 인증 객체에 들어갈 유저 정보들
	private final Long userId;
	private final String userNickName;

	public CustomUserDetails(Long userId, String userNickname) {
		this.userId = userId;
		this.userNickName = userNickname;
	}

	// Spring Security가 내부적으로 사용하는 용도
	// 인터페이스 메서드는 getUsername이지만 실제로는 userId를 문자열로 리턴
	@Override
	public String getUsername() {
		return String.valueOf(userId);
	}

	// 실제 비즈니스 로직에서 사용할 것
	public Long getUserId() { return userId; }

	public String getUserNickname() { return userNickName; }

	// 권한 로직이 아직 존재하지 않으므로 빈 리스트로 처리
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() { return Collections.emptyList(); }

	// 소셜 로그인 등으로 비밀번호를 쓰지 않을 경우 빈 문자열 리턴
	@Override
	public String getPassword() {
		return "";
	}

}