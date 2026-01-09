package com.cs.hrm.service;

import com.cs.core.vo.event.AuthPermissionEvent;

/**
 * AUTH → HRM 권한 스냅샷 동기화를 수행하는 Service 인터페이스
 * 기존 REST 호출을 대체하여 내부 Service(HrmPermissionService)를 직접 호출한다.
 */
public interface HrmPermissionSyncService {

    /**
     * 권한 생성
     * @param evt 권한 이벤트
     */
    void createPermission(AuthPermissionEvent evt);

    /**
     * 권한 업데이트
     * @param evt 권한 이벤트
     */
    void updatePermission(AuthPermissionEvent evt);

    /**
     * 권한 삭제
     * @param evt 권한 이벤트
     */
    void deletePermission(AuthPermissionEvent evt);
}
