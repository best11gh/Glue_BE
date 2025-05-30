package org.glue.glue_be.auth.jwt;

import java.util.List;
import org.glue.glue_be.user.entity.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;


// Principal에 들어갈 유저 정보를 담고있는 객체
public class CustomUserDetails implements UserDetails {

    // 인증 객체에 들어갈 유저 정보들
    private final Long userId;
    private final String userNickName;
    private final UserRole userRole;

    public CustomUserDetails(Long userId, String userNickname, UserRole userRole) {
        this.userId = userId;
        this.userNickName = userNickname;
        this.userRole = userRole;
    }

    // Spring Security가 내부적으로 사용하는 용도
    // 인터페이스 메서드는 getUsername이지만 실제로는 userId를 문자열로 리턴
    @Override
    public String getUsername() {
        return String.valueOf(userId);
    }

    // 실제 비즈니스 로직에서 사용할 것
    public Long getUserId() {
        return userId;
    }

    public String getUserNickname() {
        return userNickName;
    }

    public UserRole getRole() {
        return userRole;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userRole.getAuthority()));
    }

    // 소셜 로그인 등으로 비밀번호를 쓰지 않을 경우 빈 문자열 리턴
    @Override
    public String getPassword() {
        return "";
    }

}