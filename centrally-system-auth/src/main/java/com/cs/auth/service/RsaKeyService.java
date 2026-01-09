package com.cs.auth.service;

import java.security.KeyPair;

/**
 * RSA 키 쌍 관리 서비스 인터페이스
 * - 공개키는 클라이언트에 제공
 * - 개인키는 서버에만 보관하여 복호화에 사용
 * - 보안을 위해 주기적으로 키 갱신
 */
public interface RsaKeyService {
    
    /**
     * 현재 키 쌍 반환
     * @return 현재 사용 중인 KeyPair
     */
    KeyPair getCurrentKeyPair();
    
    /**
     * Base64로 인코딩된 공개키 반환
     * @return Base64 인코딩된 공개키 문자열
     */
    String getBase64PublicKey();
    
    /**
     * Base64로 인코딩된 개인키 반환
     * @return Base64 인코딩된 개인키 문자열
     */
    String getBase64PrivateKey();
    
    /**
     * 서비스 초기화 시 키 쌍 생성
     */
    void initialize();
    
    /**
     * 새로운 키 쌍 생성
     */
    void generateNewKeyPair();
    
    /**
     * 공개키 반환 (Base64)
     * @return Base64 인코딩된 공개키 문자열
     */
    String getPublicKey();
    
    /**
     * 비밀번호 복호화
     * @param encryptedPassword 암호화된 비밀번호
     * @return 복호화된 비밀번호
     * @throws RuntimeException 복호화 실패 시
     */
    String decryptPassword(String encryptedPassword);
    
    /**
     * 키 쌍 주기적 갱신 (1시간마다)
     * 보안 강화를 위해 키를 주기적으로 변경
     */
    void refreshKeyPair();
}
