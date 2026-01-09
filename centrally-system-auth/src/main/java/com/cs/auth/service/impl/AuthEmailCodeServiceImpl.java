package com.cs.auth.service.impl;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs.auth.entity.AuthEmailCode;
import com.cs.auth.entity.AuthUser;
import com.cs.auth.repository.AuthEmailCodeRepository;
import com.cs.auth.repository.AuthUserRepository;
import com.cs.auth.service.AuthEmailCodeService;
import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.core.service.EmailService;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.Duration;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthEmailCodeServiceImpl implements AuthEmailCodeService {

    private final AuthEmailCodeRepository authEmailCodeRepository;
    private final AuthUserRepository authUserRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder   passwordEncoder;
    private final Random random = new Random();

    /**
     * 회원가입용(동기 이메일 발송) 인증 코드 생성 & 전송
     */
    @Override
    @Transactional
    public void sendVerificationCode(String email) {
        // 6자리 랜덤 인증 코드 생성
        String code = String.format("%06d", random.nextInt(1000000));
        Timestamp now = Timestamp.from(Instant.now());
        Timestamp expiresAt = Timestamp.from(Instant.now().plus(Duration.ofMinutes(5)));

        // DB에서 해당 이메일로 저장된 기존 인증 코드 레코드 조회
        Optional<AuthEmailCode> optional = authEmailCodeRepository.findByEmail(email);
        AuthEmailCode authEmailCode;
        if (optional.isPresent()) {
            authEmailCode = optional.get();
            // 기존 레코드 업데이트
            authEmailCode.setVerificationCode(code);
            authEmailCode.setCreatedAt(now);
            authEmailCode.setExpiresAt(expiresAt);
            authEmailCode.setVerified(false);
        } else {
            // 새로운 인증 코드 레코드 생성
            authEmailCode = AuthEmailCode.builder()
                    .email(email)
                    .verificationCode(code)
                    .createdAt(now)
                    .expiresAt(expiresAt)
                    .verified(false)
                    .build();
        }
        authEmailCodeRepository.save(authEmailCode);

        // 실제 이메일 발송 로직 호출 (비동기)
//        sendEmailAsync(email, code);
        sendEmailSync(email, code);
    }

    /**
     * 비밀번호 찾기용(동기 이메일 발송) 인증 코드 생성 & 전송
     */
    @Override
    @Transactional
    public void sendVerificationCodeSync(String email) {
        // 6자리 랜덤 인증 코드 생성
        String code = String.format("%06d", random.nextInt(1000000));
        Timestamp now = Timestamp.from(Instant.now());
        Timestamp expiresAt = Timestamp.from(Instant.now().plus(Duration.ofMinutes(5)));

        Optional<AuthEmailCode> optional = authEmailCodeRepository.findByEmail(email);
        AuthEmailCode authEmailCode;
        if (optional.isPresent()) {
            authEmailCode = optional.get();
            authEmailCode.setVerificationCode(code);
            authEmailCode.setCreatedAt(now);
            authEmailCode.setExpiresAt(expiresAt);
            authEmailCode.setVerified(false);
        } else {
            authEmailCode = AuthEmailCode.builder()
                    .email(email)
                    .verificationCode(code)
                    .createdAt(now)
                    .expiresAt(expiresAt)
                    .verified(false)
                    .build();
        }
        authEmailCodeRepository.save(authEmailCode);

        // 동기 이메일 발송
        sendEmailSync(email, code);
    }

    /**
     * 비동기 이메일 전송
     */
    private void sendEmailAsync(String email, String code) {
        String subject = "[CENTRALLY] 이메일 인증 코드 안내 : " + code;
        StringBuilder content = new StringBuilder();
        content.append("안녕하세요,\n\n")
               .append("아래의 인증 코드를 입력하여 이메일 인증을 완료해 주세요.\n\n")
               .append("인증 코드: ").append(code).append("\n\n")
               .append("※ 이 코드는 5분 동안 유효합니다.\n\n")
               .append("감사합니다.\n")
               .append("CENTRALLY 팀");

        emailService.sendSimpleMessage(email, subject, content.toString());
    }

    /**
     * 동기 이메일 전송
     */
    private void sendEmailSync(String email, String code) {
        String subject = "[CENTRALLY] 이메일 인증 코드 안내 : " + code;
        StringBuilder content = new StringBuilder();
        content.append("안녕하세요,\n\n")
               .append("아래의 인증 코드를 입력하여 이메일 인증을 완료해 주세요.\n\n")
               .append("인증 코드: ").append(code).append("\n\n")
               .append("※ 이 코드는 5분 동안 유효합니다.\n\n")
               .append("감사합니다.\n")
               .append("CENTRALLY 팀");

        // 동기 방식으로 전송하여 완료 여부를 보장
        emailService.sendSimpleMessageSync(email, subject, content.toString());
    }

    
    /**
     * 소셜 로그인 매핑 완료 후 발송할 "알림용" 메일 메서드
     *  - 카카오는 나중에 알림톡 전환 예정 → 분기 처리
     *  
     *  linkedSocialEmail : 수신자
     */
    @Override
    public void sendSocialLinkNotification(String email, String socialName, String linkedSocialEmail) {
        // 카카오인 경우 → 현재는 이메일 대신 나중에 알림톡으로 보낼 것
        if ("kakao".equalsIgnoreCase(socialName)) {
            // TODO: 카카오 알림톡 발송 로직으로 전환 예정
            // 현재는 아무것도 안 하거나, 테스트용 로그만 찍기
            System.out.println("카카오 계정 매핑됨 - 추후 알림톡으로 전환 예정");
            return;
        }

        // 그 외(예: 구글) → 이메일 발송
        String subject = "[Centrally-System] 소셜 계정 연동 안내";
        StringBuilder sb = new StringBuilder();
        sb.append("안녕하세요,\n\n")
          .append("Centrally-System 계정의 [" + email + "] 아이디와\n")
          .append("소셜 (" + socialName + ") 계정이 성공적으로 연동되었습니다.\n\n")
          .append("만약 본인이 해당 아이디와 연동하지 않으셨다면, \n")
          .append("아래 번호로 즉시 문의 부탁드립니다.\n\n")
          .append("문의 전화: 010-1111-1111\n\n")
          .append("감사합니다.\nCentrally-System 팀");

        // 보내는 메일 제목은 sendEmail 내부에서 설정해도 되지만, 여기서는 통합
        emailService.sendSimpleMessage(linkedSocialEmail, subject, sb.toString());
        
        System.out.println("✅ 소셜 연동 알림 메일 발송 완료: " + email);

    }
    

    /**
     * 사용자가 입력한 인증 코드와 DB에 저장된 인증 코드를 비교하여 검증
     *
     * @param 이메일과 인증 코드 정보
     * @return 인증이 성공하면 true, 실패하면 false 반환
     */
    @Override
    @Transactional
    public boolean verifyCode(String email, String code) {
        Optional<AuthEmailCode> optional = authEmailCodeRepository.findByEmail(email);
        if (optional.isEmpty()) {
            return false;
        }
        AuthEmailCode authEmailCode = optional.get();

        // 현재 시간과 만료 시간 비교
        Timestamp now = Timestamp.from(Instant.now());
        // before 메서드는 두 날짜를 비교하여, 현재 객체가 인자로 전달한 날짜보다 이전인지 확인하는 용도로 사용
        if (authEmailCode.getExpiresAt() == null || authEmailCode.getExpiresAt().before(now)) {
            return false;
        }

        // 인증 코드 일치 여부 확인
        if (authEmailCode.getVerificationCode() != null && authEmailCode.getVerificationCode().equals(code)) {
            authEmailCode.setVerified(true);
            authEmailCodeRepository.save(authEmailCode);
            return true;
        }
        return false;
    }
    
    
    /**
     * 사용자가 입력한 메일이 인증이 되었는지(auth_email_code > verify 컬럼) 검증
    *
    * @param request 이메일과 인증 코드 정보를 담은 DTO
    * @return 인증이 되었으면 true, 아니면 false 반환
    */
    @Override
    @Transactional
	public boolean isEmailVerified(String email, String code) {
    	Optional<AuthEmailCode> optional = authEmailCodeRepository.findByEmail(email);
    	if (optional.isEmpty()) {
    		return false;
    	}
    	AuthEmailCode authEmailCode = optional.get();

    	if (authEmailCode.getVerified() == null || !authEmailCode.getVerified()) {
    		return false;
    	}
    	return true;
	}

    
    /**
     * 1) 인증 코드 검증
     * 2) 임시 비밀번호 생성
     * 3) DB 반영
     * 4) 메일 동기 발송
     */
    @Override
    @Transactional
    public void verifyCodeAndSendTempPassword(String email, String code) {

        /* 1) 인증 코드 검증 */
        boolean verified = verifyCode(email, code);
        if (!verified) {
            throw new IllegalArgumentException(GlobalExceptionHandler.CC + "인증 코드가 유효하지 않습니다.");
        }

        /* 2) 임시 비밀번호 생성 (영문/숫자 12자) */
        String tempPassword = RandomStringUtils.randomAlphanumeric(12);

        /* 3) 사용자 비밀번호 업데이트 (BCrypt 해시) */
        AuthUser user = authUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(GlobalExceptionHandler.CC + "존재하지 않는 사용자입니다."));

        user.setPassword(passwordEncoder.encode(tempPassword));
        authUserRepository.save(user);

        /* 4) 임시 비밀번호 메일 '동기' 발송 */
        sendTempPasswordEmailSync(email, tempPassword);
    }

    /** 임시 비밀번호 안내 메일 (동기) */
    private void sendTempPasswordEmailSync(String email, String tempPassword) {

        String subject = "[Centrally-System] 임시 비밀번호 안내";
        StringBuilder sb = new StringBuilder();
        sb.append("안녕하세요,\n\n")
          .append("요청하신 임시 비밀번호를 안내드립니다.\n\n")
          .append("임시 비밀번호: ").append(tempPassword).append("\n\n")
          .append("※ 최초 로그인 후 반드시 비밀번호를 변경해 주세요.\n\n")
          .append("감사합니다.\nCentrally-System 팀");

        emailService.sendSimpleMessageSync(email, subject, sb.toString());
    }
    
}

