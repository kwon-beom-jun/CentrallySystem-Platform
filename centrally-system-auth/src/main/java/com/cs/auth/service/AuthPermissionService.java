package com.cs.auth.service;

import com.cs.auth.controller.request.AuthUserPermissionPatchRequest;
import com.cs.auth.controller.response.AuthUserPermissionsResponse;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 권한 관리 서비스 인터페이스
 */
public interface AuthPermissionService {

    /**
     * 유저별 권한 정보 + 전체 권한 목록
     * @return 사용자별 권한 정보와 전체 권한 목록을 포함한 응답 DTO
     */
    @Transactional
    AuthUserPermissionsResponse getAuthUserPermissionsResponse();

    /**
     * 유저별 권한 정보 업데이트
     * @param request 권한 업데이트 요청 DTO
     * @throws IllegalArgumentException 해당 유저가 존재하지 않거나 권한이 존재하지 않는 경우
     */
    @Transactional
    void updateUserPermission(AuthUserPermissionPatchRequest request);

    /**
     * 유저 권한 추가
     * @param request 권한 추가 요청 DTO
     * @throws IllegalArgumentException 유저가 없거나 이미 해당 서비스 권한이 존재하는 경우
     */
    @Transactional
    void createUserWithRole(AuthUserPermissionPatchRequest request);

    /**
     * 유저 권한 삭제
     * @param request 권한 삭제 요청 DTO
     * @throws IllegalArgumentException 유저가 없는 경우
     */
    @Transactional
    void deleteUserWithRole(AuthUserPermissionPatchRequest request);
}
