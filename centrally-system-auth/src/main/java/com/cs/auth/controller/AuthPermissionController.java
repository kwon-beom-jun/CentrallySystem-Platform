package com.cs.auth.controller;

import com.cs.auth.controller.request.AuthUserPermissionPatchRequest;
import com.cs.auth.controller.response.AuthUserPermissionsResponse;
import com.cs.auth.service.AuthPermissionService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user-permissions")
@RequiredArgsConstructor
public class AuthPermissionController {

    private final AuthPermissionService authPermissionService;
    
    /**
     * GET /user-permissions
     * 유저 + 권한 정보를 한 번에 조회
     */
    @GetMapping
    public ResponseEntity<AuthUserPermissionsResponse> getUsersWithRoles() {
    	AuthUserPermissionsResponse response = authPermissionService.getAuthUserPermissionsResponse();
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /user-permissions
     * 특정 유저의 권한을 업데이트 (하나의 서비스에 대해 새로운 권한 할당)
     */
    @PatchMapping
    public ResponseEntity<?> patchUserWithRole(@RequestBody AuthUserPermissionPatchRequest request) {
        authPermissionService.updateUserPermission(request);
        return ResponseEntity.ok().body("권한 업데이트 성공");
    }

    /**
     * POST /user-permissions
     * 특정 유저의 권한을 추가
     */
    @PostMapping
    public ResponseEntity<?> createUserWithRole(@RequestBody AuthUserPermissionPatchRequest request) {
        authPermissionService.createUserWithRole(request);
        return ResponseEntity.ok().body("권한 업데이트 성공");
    }
    
    /**
     * DELETE /user-permissions
     * 특정 유저의 권한을 삭제
     */   
    @DeleteMapping
    public ResponseEntity<?> deleteUserWithRole(@RequestBody AuthUserPermissionPatchRequest request) {
        authPermissionService.deleteUserWithRole(request);
        return ResponseEntity.ok().body("권한 삭제 성공");
    }
}
