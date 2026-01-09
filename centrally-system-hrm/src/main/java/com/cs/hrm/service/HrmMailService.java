package com.cs.hrm.service;

import com.cs.core.kafka.event.info.PostMailRequestEvent;

/**
 * 메일 발송 전담 서비스 인터페이스
 * INFO 서비스의 게시글 메일 발송 요청 처리
 */
public interface HrmMailService {

    /**
     * INFO 게시글 메일 발송 처리
     * @param event 게시글 메일 요청 이벤트
     */
    void sendInfoPostNotificationMail(PostMailRequestEvent event);
}
