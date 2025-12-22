package com.cs.core.kafka.topic;

/**
 * INFO 서비스 → 외부 마이크로서비스로 전파되는 이벤트
 */
public final class InfoTopics {
    private InfoTopics() {}

    /**
     * 게시글 메일 발송 요청
     */
    public static final String POST_MAIL_REQUEST = "info.post.mail.request";
}

