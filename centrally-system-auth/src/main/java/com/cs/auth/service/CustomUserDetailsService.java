package com.cs.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 인증 정보 로드 서비스 인터페이스
 * 
 * 일반 로그인 처리
 * 
 * 순서
 * 	UsernamePasswordAuthenticationToken 객체를 생성하여 전달
 * 	→ AuthenticationManager는 AuthenticationProvider를 통해 유저 정보를 조회하고 인증 수행
 * 	→ AuthenticationProvider가 UserDetailsService를 사용하여 DB에서 사용자 정보를 조회
 * 	  (UserDetailsService는 SecurityConfig에서 AuthenticationProvider의 Bean 설정에 등록)
 * 	→ 내부적으로 CustomUserDetailsService의 loadUserByUsername() 메서드를 호출
 *
 * 	🔥 AuthenticationProvider (예: DaoAuthenticationProvider) 🔥
 * 		사용자가 로그인 폼에 입력한 비밀번호와, loadUserByUsername에서 반환한 UserDetails의 비밀번호(암호화된 값)를
 * 		PasswordEncoder를 이용해 비교
 * 		이 과정에서 passwordEncoder.matches(입력비밀번호, 저장된암호화비밀번호) 메서드가 호출되어 검증
 */
public interface CustomUserDetailsService extends UserDetailsService {

    /**
     * 이메일을 기반으로 사용자 인증 정보를 로드
     * @param email 사용자 이메일
     * @return 사용자 인증 정보 (UserDetails)
     * @throws org.springframework.security.core.userdetails.UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Override
    @Transactional
    UserDetails loadUserByUsername(String email);
}
