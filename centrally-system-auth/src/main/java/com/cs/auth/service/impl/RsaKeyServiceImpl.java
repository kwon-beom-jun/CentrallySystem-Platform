package com.cs.auth.service.impl;

import java.security.KeyPair;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.cs.auth.service.RsaKeyService;
import com.cs.auth.util.RsaPasswordUtil;

import jakarta.annotation.PostConstruct;
import lombok.Getter;

/**
 * RSA 키 쌍 관리 서비스 구현체
 * - 공개키는 클라이언트에 제공
 * - 개인키는 서버에만 보관하여 복호화에 사용
 * - 보안을 위해 주기적으로 키 갱신
 */
@Service
public class RsaKeyServiceImpl implements RsaKeyService {
    
    @Getter
    private KeyPair currentKeyPair;
    
    @Getter
    private String base64PublicKey;
    
    @Getter
    private String base64PrivateKey;
    
    // 키 만료 시간 관리 (선택사항)
    private final ConcurrentHashMap<String, Long> keyTimestamps = new ConcurrentHashMap<>();
    
    /**
     * 서비스 초기화 시 키 쌍 생성
     */
    @PostConstruct
    @Override
    public void initialize() {
        generateNewKeyPair();
    }
    
    /**
     * 새로운 키 쌍 생성
     */
    @Override
    public void generateNewKeyPair() {
        try {
            currentKeyPair = RsaPasswordUtil.generateKeyPair();
            base64PublicKey = RsaPasswordUtil.publicKeyToBase64(currentKeyPair.getPublic());
            base64PrivateKey = RsaPasswordUtil.privateKeyToBase64(currentKeyPair.getPrivate());
            
            String keyId = Base64.getEncoder().encodeToString(currentKeyPair.getPublic().getEncoded());
            keyTimestamps.put(keyId, System.currentTimeMillis());
            
        } catch (Exception e) {
            throw new RuntimeException("RSA 키 쌍 생성 실패", e);
        }
    }
    
    /**
     * 공개키 반환 (Base64)
     */
    @Override
    public String getPublicKey() {
        return base64PublicKey;
    }
    
    /**
     * 비밀번호 복호화
     */
    @Override
    public String decryptPassword(String encryptedPassword) {
        try {
            return RsaPasswordUtil.decrypt(encryptedPassword, base64PrivateKey);
        } catch (Exception e) {
            throw new RuntimeException("비밀번호 복호화 실패", e);
        }
    }
    
    /**
     * 키 쌍 주기적 갱신 (1시간마다)
     * 보안 강화를 위해 키를 주기적으로 변경
     */
    @Scheduled(fixedRate = 3600000) // 1시간 = 3600000ms
    @Override
    public void refreshKeyPair() {
        generateNewKeyPair();
    }
}

