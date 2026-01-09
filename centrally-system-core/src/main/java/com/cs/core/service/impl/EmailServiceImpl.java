package com.cs.core.service.impl;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.core.service.EmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${cs.mail.enabled:true}")
    private boolean mailEnabled;

    /**
     * 단일 수신자 간단 메일 (비동기)
     */
    @Override
    @Async("mailExecutor")
    public void sendSimpleMessage(String to, String subject, String text) {
        send(new String[]{to}, subject, text);
    }

    /**
     * 다중 수신자 지원 메서드 (비동기)
     */
    @Override
    @Async("mailExecutor")
    public void send(String[] to, String subject, String text) {
        if (!mailEnabled) {
            log.info("[메일 비활성화] subject={}, to={} (skip)", subject, (Object) to);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("이메일 전송 중 오류", e);
            throw new IllegalStateException(GlobalExceptionHandler.CC+"이메일 전송에 실패했습니다.");
        }
    }

    /**
     * 단일 수신자 간단 메일 (동기)
     */
    @Override
    public void sendSimpleMessageSync(String to, String subject, String text) {
        sendSync(new String[]{to}, subject, text);
    }

    /**
     * 다중 수신자 지원 메서드 (동기)
     */
    @Override
    public void sendSync(String[] to, String subject, String text) {
        if (!mailEnabled) {
            log.info("[메일 비활성화] subject={}, to={} (skip)", subject, (Object) to);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            // 동기 전송: @Async 미사용 메서드이므로 호출이 완료될 때까지 대기
            mailSender.send(message);
        } catch (Exception e) {
            log.error("이메일 전송 중 오류", e);
            throw new IllegalStateException(GlobalExceptionHandler.CC + "이메일 전송에 실패했습니다.");
        }
    }
}

