package com.cs.hrm.service.impl;

import com.cs.core.vo.event.AuthPermissionEvent;
import com.cs.hrm.controller.request.HrmUserPermissionPatchRequest;
import com.cs.hrm.service.HrmPermissionService;
import com.cs.hrm.service.HrmPermissionSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AUTH → HRM 권한 스냅샷 동기화를 수행하는 Service 구현체
 * 기존 REST 호출을 대체하여 내부 Service(HrmPermissionService)를 직접 호출한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HrmPermissionSyncServiceImpl implements HrmPermissionSyncService {

    private final HrmPermissionService permissionService;

    private HrmUserPermissionPatchRequest toReq(AuthPermissionEvent e) {
        HrmUserPermissionPatchRequest req = new HrmUserPermissionPatchRequest();
        req.setUserId(e.getUserId());
        req.setServiceName(e.getServiceName());
        req.setRoleName(e.getRoleName());
        return req;
    }

    @Override
    @Transactional
    public void createPermission(AuthPermissionEvent evt) {
        log.info("[HrmPermissionSync] create {}", evt);
        permissionService.createUserWithRole(toReq(evt));
    }

    @Override
    @Transactional
    public void updatePermission(AuthPermissionEvent evt) {
        log.info("[HrmPermissionSync] update {}", evt);
        permissionService.updateUserPermission(toReq(evt));
    }

    @Override
    @Transactional
    public void deletePermission(AuthPermissionEvent evt) {
        log.info("[HrmPermissionSync] delete {}", evt);
        permissionService.deleteUserWithRole(toReq(evt));
    }
}

