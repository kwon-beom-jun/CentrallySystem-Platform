package com.cs.auth.service;

import com.cs.auth.controller.request.AuthJoinRequest;
import com.cs.auth.entity.AuthTempUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 임시 사용자 관리 서비스 인터페이스
 * - 관리자 승인 전 임시 회원가입 처리
 * - 임시 사용자 승인 및 정식 사용자 전환
 */
public interface AuthTempUserService {

    /**
     * 모든 임시 사용자 조회
     * @return 임시 사용자 목록
     */
    @Transactional
    List<AuthTempUser> getAllUsers();

    /**
     * 임시 사용자 삭제
     * @param id 임시 사용자 ID
     * @throws RuntimeException 임시 사용자를 찾을 수 없는 경우
     */
    @Transactional
    void deleteUser(Integer id);

    /**
     * 임시 회원가입 (관리자 승인 전)
     * @param request 회원가입 요청 DTO
     * @throws IllegalArgumentException 이메일 중복, 필수 약관 미동의, 비밀번호 오류 등
     * @throws org.springframework.security.authentication.BadCredentialsException 암호화된 비밀번호가 아닌 경우
     */
    @Transactional
    void createUser(AuthJoinRequest request);

    /**
     * 임시 사용자 승인 → 정식 사용자로 이동
     * @param tempUserId 임시 사용자 ID
     * @throws RuntimeException 임시 사용자를 찾을 수 없는 경우
     */
    @Transactional
    void approveUser(Integer tempUserId);
}
