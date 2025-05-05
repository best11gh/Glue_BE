package org.glue.glue_be.auth.jwt;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


// Principal에 들어갈 유저 정보를 담고있는 객체
public class CustomUserDetails implements UserDetails {

	// 인증 객체에 들어갈 유저 정보들은 추후 협의하며 추가해갈 수 있음
	private final UUID userUuid;

	public CustomUserDetails(UUID uuid) { this.userUuid = uuid; }

	// Spring Security가 내부적으로 사용하는 용도
	// 인터페이스 따라가기에 함수명은 getUsername이지만 실제론 유저의 id를 리턴
	@Override
	public String getUsername() { return userUuid.toString(); }

	// 실제 비즈니스 로직에서 사용할 것
	public UUID getUserUuid() { return userUuid; }



	//--------- 하단 메서드들은 쓸모없지만 인터페이스 때문에 일단 구현은 필수라 적당히 넣어둔 것


	// 권한 로직이 아직 존재하지 않으므로 빈 리스트로 처리
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// 유저 객체에 넣을 권한 리스트 -> 아직 권한 세팅 안해놔서 빈 리스트
		List<GrantedAuthority> authorities = Collections.emptyList();
		return authorities;
	}


	// 우린 소셜로그인 사용하기 때문에 디비에 유저의 암호를 저장하지 않아 아마 구현할 일이 없을 메서드
	@Override
	public String getPassword() {
		return "";
	}

}