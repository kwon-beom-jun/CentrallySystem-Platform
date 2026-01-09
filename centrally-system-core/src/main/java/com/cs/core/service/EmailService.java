package com.cs.core.service;

import org.springframework.scheduling.annotation.Async;

/**
 * 이메일 발송 서비스 인터페이스
 * - 비동기 및 동기 이메일 발송 지원
 * - 단일 및 다중 수신자 지원
 */
public interface EmailService {

    /**
     * 단일 수신자 간단 메일 (비동기)
     * @param to 수신자 이메일 주소
     * @param subject 메일 제목
     * @param text 메일 본문
     */
    @Async("mailExecutor")
    void sendSimpleMessage(String to, String subject, String text);

    /**
     * 다중 수신자 지원 메서드 (비동기)
     * @param to 수신자 이메일 주소 배열
     * @param subject 메일 제목
     * @param text 메일 본문
     */
    @Async("mailExecutor")
    void send(String[] to, String subject, String text);

    /**
     * 단일 수신자 간단 메일 (동기)
     * @param to 수신자 이메일 주소
     * @param subject 메일 제목
     * @param text 메일 본문
     */
    void sendSimpleMessageSync(String to, String subject, String text);

    /**
     * 다중 수신자 지원 메서드 (동기)
     * @param to 수신자 이메일 주소 배열
     * @param subject 메일 제목
     * @param text 메일 본문
     */
    void sendSync(String[] to, String subject, String text);
}
