package com.cs.auth.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs.auth.controller.request.AuthJoinRequest;
import com.cs.auth.entity.Agreement;
import com.cs.auth.entity.AuthTempUser;
// 포트폴리오용: 사용하지 않는 import 주석 처리
// import com.cs.auth.entity.TempUserAgreement;
import com.cs.auth.repository.AgreementRepository;
import com.cs.auth.repository.AuthTempUserRepository;
import com.cs.auth.repository.AuthUserRepository;
import com.cs.auth.repository.TempUserAgreementRepository;
import com.cs.auth.service.AuthTempUserService;
import com.cs.auth.service.AuthUserService;
import com.cs.auth.service.RsaKeyService;
import com.cs.core.handler.GlobalExceptionHandler;
import org.springframework.security.authentication.BadCredentialsException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthTempUserServiceImpl implements AuthTempUserService {

    private final PasswordEncoder passwordEncoder;
    private final AuthTempUserRepository authTempUserRepository;
    private final AuthUserRepository authUserRepository;
    private final AuthUserService authUserService;
    private final RsaKeyService rsaKeyService;
    private final AgreementRepository agreementRepository;
    private final TempUserAgreementRepository tempUserAgreementRepository;
    private static final String DEFAULT_LANGUAGE_CODE = "ko";

    /**
     * 모든 사용자 조회
     */
    @Override
    @Transactional
    public List<AuthTempUser> getAllUsers() {
        return authTempUserRepository.findAll();
    }

    /**
     * 삭제
     */
    @Override
    @Transactional
    public void deleteUser(Integer id) {
    	AuthTempUser user = authTempUserRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "Temp User not found: " + id));
    	authTempUserRepository.delete(user);
    }
    
    /**
     * 임시 회원가입 (관리자 승인 전)
     */
    @Override
    @Transactional
    public void createUser(AuthJoinRequest request) {
        // 0) 이메일 중복 체크
        if (authUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(GlobalExceptionHandler.CC + "이미 사용 중인 이메일입니다.");
        }

        if (authTempUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(GlobalExceptionHandler.CC + "이미 회원가입 되어있습니다\n관리자 승인이 필요합니다");
        }

        // 1) 필수 약관 검증
        String languageCode = resolveLanguageCode(request.getAgreementLanguageCode());
        List<Agreement> requiredAgreements = loadRequiredAgreements(languageCode);

        Map<Long, Boolean> consentMap = request.getAgreements() == null ?
                java.util.Collections.emptyMap() :
                request.getAgreements().stream()
                        .collect(java.util.stream.Collectors.toMap(
                                com.cs.auth.controller.request.AgreementRequest::getAgreementId,
                                com.cs.auth.controller.request.AgreementRequest::getIsAgreed));

        boolean allRequiredAgreed = requiredAgreements.stream()
                .allMatch(ag -> consentMap.getOrDefault(ag.getAgreementId(), false));

        if (!allRequiredAgreed) {
            throw new IllegalArgumentException(GlobalExceptionHandler.CC + "필수 약관에 동의해야 합니다.");
        }

        // 포트폴리오용: 기능 비활성화
        throw new RuntimeException("포트폴리오용: 회원가입 기능이 비활성화되어 있습니다.");
    }

    /**
     * 요청 언어에 대응하는 필수 약관 목록을 반환한다.
     */
    private List<Agreement> loadRequiredAgreements(String languageCode) {
        List<Agreement> requiredAgreements = agreementRepository.findByLanguageCodeAndIsRequiredTrue(languageCode);
        if (requiredAgreements.isEmpty() && !DEFAULT_LANGUAGE_CODE.equals(languageCode)) {
            requiredAgreements = agreementRepository.findByLanguageCodeAndIsRequiredTrue(DEFAULT_LANGUAGE_CODE);
        }
        return requiredAgreements;
    }

    /**
     * 언어 코드가 비어 있을 경우 기본 언어 코드로 대체한다.
     */
    private String resolveLanguageCode(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return DEFAULT_LANGUAGE_CODE;
        }
        return languageCode.toLowerCase();
    }


    /**
     * 임시 사용자 승인 → 정식 사용자로 이동
     */
    @Override
    @Transactional
    public void approveUser(Integer tempUserId) {

        // 1) 임시 사용자 조회
        AuthTempUser t = authTempUserRepository.findById(tempUserId)
            .orElseThrow(() -> new RuntimeException(
                GlobalExceptionHandler.CC + "Temp User not found: " + tempUserId));

        // 2) 정식 회원 가입 요청 DTO 구성
        AuthJoinRequest joinReq = AuthJoinRequest.builder()
            .email(t.getEmail())
            .password(t.getPassword())
            .userName(t.getName())
            .phone(t.getPhoneNumber())
            .build();

        // 3) AuthUserService 를 통해 정식 사용자 생성
        authUserService.createUser(joinReq);

        // 4) 임시 사용자 레코드 삭제
        authTempUserRepository.delete(t);
    }
    
    /**
     * Base64 인코딩된 문자열인지 확인
     */
    private boolean isBase64Encoded(String str) {
        try {
            java.util.Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
}

