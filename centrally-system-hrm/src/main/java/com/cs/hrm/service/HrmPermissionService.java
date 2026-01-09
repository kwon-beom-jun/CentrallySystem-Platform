package com.cs.hrm.service;

import com.cs.hrm.controller.request.HrmUserPermissionPatchRequest;
import com.cs.hrm.controller.response.HrmUserPermissionsResponse;

/**
 * 권한 서비스 인터페이스
 */
public interface HrmPermissionService {

    /**
     * 사용자 권한 정보 조회
     * @return 사용자 권한 응답
     */
    HrmUserPermissionsResponse getHrmUserPermissionsResponse();

    /**
     * 사용자 권한 업데이트
     * @param req 권한 패치 요청
     */
    void updateUserPermission(HrmUserPermissionPatchRequest req);

    /**
     * 사용자 권한 생성
     * @param req 권한 패치 요청
     */
    void createUserWithRole(HrmUserPermissionPatchRequest req);

    /**
     * 사용자 권한 삭제
     * @param req 권한 패치 요청
     */
    void deleteUserWithRole(HrmUserPermissionPatchRequest req);
}
