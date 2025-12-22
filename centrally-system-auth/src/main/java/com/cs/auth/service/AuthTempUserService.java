package com.cs.auth.service;

import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs.auth.controller.request.AuthJoinRequest;
import com.cs.auth.entity.Agreement;
import com.cs.auth.entity.AuthTempUser;
import com.cs.auth.entity.TempUserAgreement;
import com.cs.auth.repository.AgreementRepository;
import com.cs.auth.repository.AuthTempUserRepository;
import com.cs.auth.repository.AuthUserRepository;
import com.cs.auth.repository.TempUserAgreementRepository;
import com.cs.core.handler.GlobalExceptionHandler;
import org.springframework.security.authentication.BadCredentialsException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthTempUserService {

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
    @Transactional
    public List<AuthTempUser> getAllUsers() {
        return authTempUserRepository.findAll();
    }

    /**
     * 삭제
     */
    @Transactional
    public void deleteUser(Integer id) {
    	AuthTempUser user = authTempUserRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "Temp User not found: " + id));
    	authTempUserRepository.delete(user);
    }
    
    /**
     * 임시 회원가입 (관리자 승인 전)
     */
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

        // 2) 비밀번호 복호화 (RSA 암호화 필수)
        String password = request.getPassword();
        if (password == null || password.isEmpty()) {
            throw new BadCredentialsException(
                GlobalExceptionHandler.CC + "비밀번호가 필요합니다."
            );
        }
        
        try {
            // RSA 암호화된 비밀번호인지 확인
            if (password.length() < 200 || !isBase64Encoded(password)) {
                throw new BadCredentialsException(
                    GlobalExceptionHandler.CC + "암호화된 비밀번호가 필요합니다. 평문 비밀번호는 허용되지 않습니다."
                );
            }
            
            // RSA 암호화된 비밀번호 복호화
            password = rsaKeyService.decryptPassword(password);
        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                GlobalExceptionHandler.CC + "비밀번호 복호화에 실패했습니다."
            );
        }
        
        // 3) 임시 사용자 저장 (BCrypt 해싱)
        AuthTempUser user = AuthTempUser.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(password))
                .name(request.getUserName())
                .phoneNumber(request.getPhone())
                .build();

        authTempUserRepository.save(user);

        // 3) 동의 내역 저장
        if (request.getAgreements() != null) {
            request.getAgreements().stream()
                    .filter(com.cs.auth.controller.request.AgreementRequest::getIsAgreed)
                    .forEach(ar -> {
                        TempUserAgreement tua = TempUserAgreement.builder()
                                .userEmail(user.getEmail())
                                .userName(user.getName())
                                .agreementId(ar.getAgreementId())
                                .isAgreed(true)
                                .build();
                        tempUserAgreementRepository.save(tua);
                    });
        }
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
