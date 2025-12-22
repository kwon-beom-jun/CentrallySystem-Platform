package com.cs.auth.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs.auth.entity.AuthRoles;
import com.cs.auth.entity.AuthUser;
import com.cs.auth.repository.AuthUserRepository;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *	일반 로그인 처리
 * 
 *	순서
 *		UsernamePasswordAuthenticationToken 객체를 생성하여 전달
 *		→ AuthenticationManager는 AuthenticationProvider를 통해 유저 정보를 조회하고 인증 수행
 *		→ AuthenticationProvider가 UserDetailsService를 사용하여 DB에서 사용자 정보를 조회
 *		  (UserDetailsService는 SecurityConfig에서 AuthenticationProvider의 Bean 설정에 등록)
 *		→ 내부적으로 CustomUserDetailsService의 loadUserByUsername() 메서드를 호출
 *
 *		🔥 AuthenticationProvider (예: DaoAuthenticationProvider) 🔥
 *			사용자가 로그인 폼에 입력한 비밀번호와, loadUserByUsername에서 반환한 UserDetails의 비밀번호(암호화된 값)를
 *			PasswordEncoder를 이용해 비교
 *			이 과정에서 passwordEncoder.matches(입력비밀번호, 저장된암호화비밀번호) 메서드가 호출되어 검증
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthUserRepository authUsersRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<AuthUser> optionalUser = authUsersRepository.findByEmail(email);
        AuthUser userEntity = optionalUser
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // 1) 유저가 가진 Roles 가져오기
//        Set<AuthRoles> roles = userEntity.getRoles();
        Set<AuthRoles> roles = userEntity.getUserRoles().stream()
                .map(authUserRole -> authUserRole.getRole())
                .collect(Collectors.toSet());

        // 2) 스프링 시큐리티용 Authority 리스트로 변환
        Set<GrantedAuthority> authorities = roles.stream()
                .map(r -> new SimpleGrantedAuthority(r.getRoleName())) // e.g. "ROLE_USER"
                .collect(Collectors.toSet());

        // 3) UserDetails 객체 생성
        return User.builder()
                .username(userEntity.getEmail())
                .password(userEntity.getPassword())
                .authorities(authorities) // 여러 권한 주입
                .build();
    }
}
