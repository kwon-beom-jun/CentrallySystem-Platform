package com.cs.auth.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cs.auth.controller.request.AuthJoinRequest;
import com.cs.auth.entity.AuthTempUser;
import com.cs.auth.entity.AuthUser;
import com.cs.auth.service.AuthEmailCodeService;
import com.cs.auth.service.AuthTempUserService;
import com.cs.core.handler.GlobalExceptionHandler;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users/temp")
@RequiredArgsConstructor
public class AuthTempUserController {
	
	private final AuthTempUserService authTempUserService;
    private final AuthEmailCodeService authEmailCodeService;


    /**
     * 모든 임시 사용자 목록 조회
     * GET /auth/users/temp
     */
    @GetMapping
    public ResponseEntity<List<AuthTempUser>> getUsers() {
        List<AuthTempUser> users = authTempUserService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * 특정 임시 사용자 삭제 (DELETE)
     * DELETE /users/{id}/temp
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Integer id) {
        try {
        	authTempUserService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "임시 사용자 삭제 성공!"));
        } catch (Exception e) {
        	throw new RuntimeException(GlobalExceptionHandler.CC + "임시 사용자 삭제 실패: " + e.getMessage());
        }
    }
    

    /**
     * 임시 사용자 추가 (관리자 승인 전)
     * POST /users/temp/join
     *
     * 회원가입은 인증 없이 진행되지만, CORS 정책으로 인해 분리된 URL 설정
     */
    @PostMapping("/join")
    public ResponseEntity<?> createUser(@RequestBody AuthJoinRequest request) {
        // 이메일 인증 검증
        if (!authEmailCodeService.isEmailVerified(request.getEmail(), request.getVerificationCode())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "메일 인증이 유효하지 않습니다. 다시 시도해 주세요."));
        }
    	authTempUserService.createUser(request);
        return ResponseEntity.ok(Map.of("message", "임시 회원가입 성공\n관리자의 승인이 필요합니다"));
    }
    


    /**
     * 임시 사용자 승인
     * POST /users/temp/{id}/approve
     *
     * 회원가입은 인증 없이 진행되지만, CORS 정책으로 인해 분리된 URL 설정
     */
	@PostMapping("/{id}/approve")
	public ResponseEntity<?> approveUser(@PathVariable("id") Integer id) {
	    try {
	        authTempUserService.approveUser(id);
	        return ResponseEntity.ok(Map.of("message", "임시 사용자 승인 완료!"));
	    } catch (Exception e) {
	        throw new RuntimeException(
	            GlobalExceptionHandler.CC + "임시 사용자 승인 실패: " + e.getMessage()
	        );
	    }
	}
    
}
