package com.cs.auth.util.social;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SocialLoginCryptoUtil {
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // GCM 태그 길이
    private static final int IV_LENGTH = 12; // GCM IV 길이

    // 🔹 256비트 AES 키를 설정 (포트폴리오용: 더미값으로 교체)
    // 원본 키는 실제 운영 환경에서 별도 Key 관리 시스템을 통해 관리해야 함
    private static final String SECRET_KEY = "PORTFOLIO_DUMMY_SECRET_KEY_256BIT_1234567890123456"; // 32 바이트 (256비트) - 더미값

    // 암호화 메서드
    public static String encrypt(String plainText) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);

        SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // IV와 암호문을 Base64로 인코딩하여 함께 전달
        // Base64.getUrlEncoder().withoutPadding()으로 변경하면 공백 없이 깨끗한 문자열이 생성
        return Base64.getUrlEncoder().withoutPadding().encodeToString(iv) + ":" 
        	 + Base64.getUrlEncoder().withoutPadding().encodeToString(cipherText);

    }

    // 복호화 메서드
    public static String decrypt(String encryptedText) throws Exception {
        String[] parts = encryptedText.split(":");
        byte[] iv = Base64.getUrlDecoder().decode(parts[0]);
        byte[] cipherText = Base64.getUrlDecoder().decode(parts[1]);

        SecretKey key = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText, StandardCharsets.UTF_8);
    }
    
    
    // 소셜 데이터 암호화
    public static String oauthDataJsonEncrypt (String social, String[] oauthDatas) {
    	
    	Map<String, String> dataMap = new HashMap<>();
    	String encryptedData = null;
    	
    	// TODO : 기준은 3개지만 나중에 바뀌면 소셜마다 각각 해줘야함
    	if (oauthDatas == null || oauthDatas.length != 3) {
			throw new RuntimeException("소셜 로그인 데이터가 없습니다");
		}
    	
    	if (social.equals("google")) {
    		dataMap.put("social", "google");
            dataMap.put("googleSubject", oauthDatas[0]);
            dataMap.put("nickname", oauthDatas[1]);
            dataMap.put("userEmail", oauthDatas[2]);
			
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonData;
            
			try {
				jsonData = objectMapper.writeValueAsString(dataMap);
				// 암호화
				encryptedData = SocialLoginCryptoUtil.encrypt(jsonData);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("소셜 데이터를 암호화 하다 실패하였습니다");
			}
			
		} else if (social.equals("kakao")) {
    		dataMap.put("social", "kakao");
            dataMap.put("kakaoId", oauthDatas[0]);
            dataMap.put("userEmail", oauthDatas[1]);
            dataMap.put("nickname", oauthDatas[2]);
			
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonData;
            
			try {
				jsonData = objectMapper.writeValueAsString(dataMap);
				// 암호화
				encryptedData = SocialLoginCryptoUtil.encrypt(jsonData);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException("소셜 데이터를 암호화 하다 실패하였습니다");
			}
		}
    	
        System.out.println("🔐 암호화된 데이터: " + encryptedData);
		return encryptedData;
    }
    
    // 소셜 데이터 복호화
    public static Map<String, String> oauthDataJsonDecrypt (String encryptedData) {
    	
    	Map<String, String> oauthDecryptedMap = null;
    	
    	try {
			String decryptedData = SocialLoginCryptoUtil.decrypt(encryptedData);
			
			// (3) JSON 데이터 출력
            System.out.println("🔓 복호화된 데이터: " + decryptedData);
            
            // (4) JSON을 다시 Map 형태로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            oauthDecryptedMap = objectMapper.readValue(decryptedData, Map.class);
            
            if (oauthDecryptedMap.get("social").equals("google")) {
                System.out.println("✅ 소셜 이름: " + oauthDecryptedMap.get("googleSubject"));
                System.out.println("✅ 소셜 사용자 ID: " + oauthDecryptedMap.get("nickname"));
                System.out.println("✅ 사용자 이메일: " + oauthDecryptedMap.get("userEmail"));
			} else if (oauthDecryptedMap.get("social").equals("kakao")) {
	            System.out.println("✅ 소셜 이름: " + oauthDecryptedMap.get("kakaoId"));
	            System.out.println("✅ 소셜 사용자 ID: " + oauthDecryptedMap.get("userEmail"));
	            System.out.println("✅ 사용자 이메일: " + oauthDecryptedMap.get("nickname"));
			}
            
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("소셜 데이터를 복호화 하다 실패하였습니다");
		}

    	return oauthDecryptedMap;
    }
}
