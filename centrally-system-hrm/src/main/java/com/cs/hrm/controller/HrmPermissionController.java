package com.cs.hrm.controller;

import com.cs.hrm.controller.request.HrmUserPermissionPatchRequest;
import com.cs.hrm.controller.response.HrmUserPermissionsResponse;
import com.cs.hrm.service.HrmPermissionService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-permissions")
@RequiredArgsConstructor
public class HrmPermissionController {

    private final HrmPermissionService service;

    @GetMapping
    public ResponseEntity<HrmUserPermissionsResponse> getUsersWithRoles() {
        return ResponseEntity.ok(service.getHrmUserPermissionsResponse());
    }

    @PatchMapping
    public ResponseEntity<?> patchUserWithRole(@RequestBody HrmUserPermissionPatchRequest req) {
        service.updateUserPermission(req);
        return ResponseEntity.ok("권한 스냅샷 업데이트 성공");
    }

    @PostMapping
    public ResponseEntity<?> createUserWithRole(@RequestBody HrmUserPermissionPatchRequest req) {
        service.createUserWithRole(req);
        return ResponseEntity.ok("권한 스냅샷 추가 성공");
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUserWithRole(@RequestBody HrmUserPermissionPatchRequest req) {
        service.deleteUserWithRole(req);
        return ResponseEntity.ok("권한 스냅샷 삭제 성공");
    }
}
