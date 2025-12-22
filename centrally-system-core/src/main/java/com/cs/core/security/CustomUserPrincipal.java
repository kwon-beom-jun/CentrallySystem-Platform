package com.cs.core.security;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomUserPrincipal implements UserDetails {

    private final String email;		// 로그인 시 사용되는 고유 식별자
    private final String username;	// 사용자 이름
    private final int userId;		// 사용자 ID
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserPrincipal(
            String email,
            String username,
            int userId,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.email       = email;
        this.username    = username;
        this.userId      = userId;
        this.authorities = authorities;
    }

    @Override
    public String getUsername() {
    	// UserDetails에서 username은 주로 식별자로 사용
        return email;
    }

    @Override
    public String getPassword() {
        return null; // 필요 없다면 null 반환
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    // 아래 메서드들은 필요한 경우 true로 설정
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }
}
