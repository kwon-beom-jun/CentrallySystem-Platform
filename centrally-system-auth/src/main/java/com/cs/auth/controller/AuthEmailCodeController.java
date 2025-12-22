package com.cs.auth.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cs.auth.controller.request.AuthEmailCodeVerificationRequest;
import com.cs.auth.controller.request.AuthEmailRequest;
import com.cs.auth.repository.AuthUserRepository;
import com.cs.auth.service.AuthEmailCodeService;
import com.cs.core.handler.GlobalExceptionHandler;

import java.util.Map;

/**
 * 
 * 인증코드를 생성하여 이메일을 발송하고 인증 코드를 확인하는 컨트롤러
 * 
 */
@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class AuthEmailCodeController {

    private final AuthEmailCodeService authEmailCodeService;
    private final AuthUserRepository authUserRepository;

    // 이메일 인증 코드 발송 요청
    @PostMapping("/send")
    public ResponseEntity<?> sendVerificationCode(@RequestBody AuthEmailRequest emailRequest) {
        String email = emailRequest.getEmail();

        // 1) 이메일로 사용자 존재 여부 확인
        boolean userExists = authUserRepository.existsByEmail(email);
        if (userExists) {
        	throw new IllegalArgumentException(GlobalExceptionHandler.CC + "이미 사용 중인 이메일입니다.");
        }
        
        authEmailCodeService.sendVerificationCode(email);
        return ResponseEntity.ok(Map.of("message", "인증 코드가 발송되었습니다."));
    }

    // 이메일 인증 코드 검증 요청
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestBody AuthEmailCodeVerificationRequest request) {
        boolean isValid = authEmailCodeService.verifyCode(request.getEmail(), request.getCode());
        if (isValid) {
            return ResponseEntity.ok(Map.of("message", "이메일 인증 성공"));
        } else {
        	throw new IllegalArgumentException(GlobalExceptionHandler.CC + "인증 코드 실패");
        }
    }

    // 비밀번호 찾기용 인증 코드 발송
    @PostMapping("/reset/send")
    public ResponseEntity<?> sendResetPasswordCode(@RequestBody AuthEmailRequest emailRequest) {
        String email = emailRequest.getEmail();

        // 1) 이메일로 사용자 존재 여부 확인
        boolean userExists = authUserRepository.existsByEmail(email);
        if (!userExists) {
            throw new IllegalArgumentException(GlobalExceptionHandler.CC + "입력하신 이메일 정보를 찾을 수 없습니다.");
        }

        authEmailCodeService.sendVerificationCodeSync(email);
        return ResponseEntity.ok(Map.of("message", "인증 코드가 발송되었습니다."));
    }
    

    // 비밀번호 초기화용 인증 코드 검증 + 임시 비밀번호 발급
    @PostMapping("/reset/confirm")
    public ResponseEntity<?> confirmResetPassword(@RequestBody AuthEmailCodeVerificationRequest request) {

        authEmailCodeService.verifyCodeAndSendTempPassword(request.getEmail(), request.getCode());

        return ResponseEntity.ok(Map.of("message", "임시 비밀번호가 이메일로 전송되었습니다.")
        );
    }
}
