package com.cs.hrm.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.cs.core.kafka.group.Group;
import com.cs.core.kafka.topic.InfoTopics;
import com.cs.core.kafka.event.info.PostMailRequestEvent;
import com.cs.hrm.service.HrmMailService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * INFO 서비스에서 발행한 게시글 메일 요청 이벤트를 수신
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InfoEventConsumer {

    private final HrmMailService hrmMailService;
    private final ObjectMapper objectMapper;

    /**
     * INFO 게시글 메일 발송 요청 수신
     */
    @KafkaListener(
        topics = InfoTopics.POST_MAIL_REQUEST,
        groupId = Group.HRM_GROUP,
        containerFactory = "kafkaListenerContainerFactoryHrmEmployment"
    )
    public void onPostMailRequest(String message, Acknowledgment ack) {
        try {
            PostMailRequestEvent event = objectMapper.readValue(message, PostMailRequestEvent.class);
            log.info("=================================================================================");
            log.info("[Kafka-Consume][HRM] {} : {}", InfoTopics.POST_MAIL_REQUEST, event);
            log.info("=================================================================================");
            
            hrmMailService.sendInfoPostNotificationMail(event);
            
            // 처리 완료 후 Kafka offset commit
            ack.acknowledge();
        } catch (Exception ex) {
            log.info("=================================================================================");
            log.error("[Kafka-Consume][HRM] Info post mail request parse error", ex);
            log.info("=================================================================================");
            // 에러 발생 시 commit 안 함 → 재시작 후 다시 처리
            throw new RuntimeException(ex);
        }
    }
}

