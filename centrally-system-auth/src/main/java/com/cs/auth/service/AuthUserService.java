package com.cs.auth.service;

import com.cs.auth.controller.request.AuthJoinRequest;
import com.cs.auth.controller.response.AuthUserSocialResponse;
import com.cs.auth.controller.response.AuthUserWithRolesResponse;
import com.cs.auth.entity.AuthUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AuthUser 관련 CRUD 서비스 인터페이스
 */
public interface AuthUserService {

    /**
     * 모든 사용자 조회
     * @return 사용자 목록
     */
    @Transactional
    List<AuthUser> getAllUsers();

    /**
     * ID로 사용자 조회
     * @param id 사용자 ID
     * @return 사용자 Optional
     */
    @Transactional
    Optional<AuthUser> getUserById(Integer id);

    /**
     * 이메일로 사용자 조회
     * @param email 이메일 주소
     * @return 사용자 Optional
     */
    @Transactional
    Optional<AuthUser> getUserByEmail(String email);

    /**
     * 사용자 ID로 사용자 정보와 권한 목록 함께 조회
     * @param userId 사용자 ID
     * @return AuthUserWithRolesResponse DTO
     * @throws RuntimeException 사용자를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    AuthUserWithRolesResponse getUserWithRoles(Integer userId);

    /**
     * 회원가입
     * @param request 회원가입 요청 DTO
     * @throws IllegalArgumentException 이미 사용 중인 이메일인 경우
     */
    @Transactional
    void createUser(AuthJoinRequest request);

    /**
     * putUser (전체 업데이트)
     * @param id 사용자 ID
     * @param request 업데이트할 사용자 엔티티
     * @throws RuntimeException 사용자를 찾을 수 없는 경우
     */
    @Transactional
    void putUser(Integer id, AuthUser request);

    /**
     * patchUser (일부 업데이트)
     * @param id 사용자 ID
     * @param patchData 업데이트할 필드와 값의 맵
     * @throws RuntimeException 사용자를 찾을 수 없는 경우
     * @throws IllegalArgumentException 현재 비밀번호가 일치하지 않는 경우
     */
    @Transactional
    void patchUser(Integer id, Map<String, Object> patchData);

    /**
     * 삭제 (Soft-Delete)
     * @param id 사용자 ID
     * @throws RuntimeException 사용자를 찾을 수 없는 경우
     */
    @Transactional
    void deleteUser(Integer id);

    /**
     * 이메일로 사용자와 소셜 로그인 정보 조회
     * @param email 이메일 주소
     * @return 사용자와 소셜 로그인 정보를 포함한 응답 DTO Optional
     */
    Optional<AuthUserSocialResponse> getUserSocialByEmail(String email);

    /**
     * 사용자의 소셜 로그인 정보 삭제
     * @param userId 사용자 ID (문자열)
     * @param socialName 소셜 로그인 제공자 이름
     * @throws RuntimeException 소셜 정보가 존재하지 않는 경우
     */
    @Transactional
    void deleteUserSocial(String userId, String socialName);
}
