package com.cs.auth.service;

import org.springframework.transaction.annotation.Transactional;

/**
 * 이메일 인증 코드 서비스 인터페이스
 * - 회원가입 및 비밀번호 찾기 시 이메일 인증 코드 발송 및 검증
 * - 소셜 로그인 연동 알림 메일 발송
 */
public interface AuthEmailCodeService {

    /**
     * 회원가입용(동기 이메일 발송) 인증 코드 생성 & 전송
     * @param email 인증 코드를 받을 이메일 주소
     */
    @Transactional
    void sendVerificationCode(String email);

    /**
     * 비밀번호 찾기용(동기 이메일 발송) 인증 코드 생성 & 전송
     * @param email 인증 코드를 받을 이메일 주소
     */
    @Transactional
    void sendVerificationCodeSync(String email);

    /**
     * 소셜 로그인 매핑 완료 후 발송할 "알림용" 메일 메서드
     * - 카카오는 나중에 알림톡 전환 예정 → 분기 처리
     * @param email Centrally-System 계정 이메일
     * @param socialName 소셜 로그인 제공자 이름 (google, kakao 등)
     * @param linkedSocialEmail 소셜 계정 이메일 (수신자)
     */
    void sendSocialLinkNotification(String email, String socialName, String linkedSocialEmail);

    /**
     * 사용자가 입력한 인증 코드와 DB에 저장된 인증 코드를 비교하여 검증
     * @param email 이메일 주소
     * @param code 사용자가 입력한 인증 코드
     * @return 인증이 성공하면 true, 실패하면 false 반환
     */
    @Transactional
    boolean verifyCode(String email, String code);

    /**
     * 사용자가 입력한 메일이 인증이 되었는지(auth_email_code > verify 컬럼) 검증
     * @param email 이메일 주소
     * @param code 인증 코드
     * @return 인증이 되었으면 true, 아니면 false 반환
     */
    @Transactional
    boolean isEmailVerified(String email, String code);

    /**
     * 인증 코드 검증 후 임시 비밀번호 생성 및 전송
     * 1) 인증 코드 검증
     * 2) 임시 비밀번호 생성
     * 3) DB 반영
     * 4) 메일 동기 발송
     * @param email 이메일 주소
     * @param code 인증 코드
     * @throws IllegalArgumentException 인증 코드가 유효하지 않거나 사용자를 찾을 수 없는 경우
     */
    @Transactional
    void verifyCodeAndSendTempPassword(String email, String code);
}
