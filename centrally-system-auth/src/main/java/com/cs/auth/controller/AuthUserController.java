package com.cs.auth.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cs.auth.controller.request.AuthJoinRequest;
import com.cs.auth.controller.response.AuthUserSocialResponse;
import com.cs.auth.controller.response.AuthUserWithRolesResponse;
import com.cs.auth.entity.AuthUser;
import com.cs.auth.service.AuthEmailCodeService;
import com.cs.auth.service.AuthUserService;
import com.cs.core.handler.GlobalExceptionHandler;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AuthUserController {

    private final AuthUserService authUserService;
    private final AuthEmailCodeService authEmailCodeService;

    /**
     * 모든 사용자 목록 조회
     * GET /auth/users
     */
    @GetMapping
    public ResponseEntity<List<AuthUser>> getUsers() {
        List<AuthUser> users = authUserService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 사용자 ID로 조회
     * GET /auth/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuthUser> getUserById(@PathVariable("id") Integer id) {
        return authUserService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 이메일로 사용자 조회
     * GET /auth/users/search?email={email}
     */
    @GetMapping("/search")
    public ResponseEntity<AuthUser> getUserByEmail(@RequestParam String email) {
        return authUserService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 사용자 ID로 사용자 정보와 권한 목록 조회
     * GET /users/{id}/with-roles
     */
    @GetMapping("/{id}/with-roles")
    public ResponseEntity<AuthUserWithRolesResponse> getUserWithRoles(@PathVariable("id") Integer id) {
        AuthUserWithRolesResponse userWithRoles = authUserService.getUserWithRoles(id);
        return ResponseEntity.ok(userWithRoles);
    }

    /**
     * 여러 사용자 조회 (Bulk Lookup)
     * POST /users/bulk-lookup
     * 요청 바디로 사용자 ID 목록을 받아 해당 사용자들을 반환합니다.
     */
//    @PostMapping("/bulk-lookup")
//    public ResponseEntity<List<AuthUser>> getUsersByIds(@RequestBody List<Integer> ids) {
//        List<AuthUser> users = authUserService.getUsersByIds(ids);
//        return ResponseEntity.ok(users);
//    }

    /**
     * 새 사용자 추가 (회원가입)
     * POST /users/join
     *
     * 회원가입은 인증 없이 진행되지만, CORS 정책으로 인해 분리된 URL 설정
     */
    @PostMapping("/join")
    public ResponseEntity<?> createUser(@RequestBody AuthJoinRequest request) {
//        try {
            authUserService.createUser(request);
            return ResponseEntity.ok(Map.of("message", "회원가입 성공!"));
//        } catch (Exception e) {
//        	throw new RuntimeException(GlobalExceptionHandler.CC + "회원가입 실패: " + e.getMessage());
//        }
    }

    /**
     * 특정 사용자 전체 업데이트 (PUT)
     * PUT /users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> putUser(@PathVariable("id") Integer id, @RequestBody AuthUser request) {
        try {
            authUserService.putUser(id, request);
            return ResponseEntity.ok(Map.of("message", "사용자 정보 전체 업데이트 성공!"));
        } catch (Exception e) {
        	throw new RuntimeException(GlobalExceptionHandler.CC + "사용자 정보 업데이트 실패 : " + e.getMessage());
        }
    }

    /**
     * 특정 사용자 일부 업데이트 (PATCH)
     * PATCH /users/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> patchUser(@PathVariable("id") Integer id, @RequestBody Map<String, Object> patchData) {
        try {
            authUserService.patchUser(id, patchData);
            return ResponseEntity.ok(Map.of("message", "사용자 정보 일부 업데이트 성공!"));
        } catch (Exception e) {
        	throw new RuntimeException(GlobalExceptionHandler.CC + "사용자 정보 업데이트 실패 : " + e.getMessage());
        }
    }

    /**
     * 특정 사용자 삭제 (DELETE)
     * DELETE /users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Integer id) {
        try {
            authUserService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "사용자 삭제 성공!"));
        } catch (Exception e) {
        	throw new RuntimeException(GlobalExceptionHandler.CC + "사용자 삭제 실패: " + e.getMessage());
        }
    }
    
    // =========================================================================================
    // ======================================== 소셜 관리 ========================================
    // =========================================================================================

    /**
     * 사용자 email로 사용자 및 소셜 정보 조회
     * GET /auth/users/{email/social}
     */
    @GetMapping("/{email}/social")
    public ResponseEntity<AuthUserSocialResponse> getUserSocialByEmail(@PathVariable("email") String email) {
        return authUserService.getUserSocialByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 사용자 id와 소셜 id로 소셜 정보 삭제
     * DELETE /auth/users/{email/social}
     */
    @DeleteMapping("/{userId}/social/{socialName}")
    public ResponseEntity<?> deleteUserSocial(
    		@PathVariable("userId") String userId,
    		@PathVariable("socialName") String socialName
		) {
    	authUserService.deleteUserSocial(userId, socialName);
        return ResponseEntity.ok(Map.of("message", "소셜 삭제 성공"));
    }
    
    
}
