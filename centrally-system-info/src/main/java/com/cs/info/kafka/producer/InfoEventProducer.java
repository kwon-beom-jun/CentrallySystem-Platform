package com.cs.info.kafka.producer;

import org.springframework.stereotype.Service;

import com.cs.core.kafka.event.info.PostMailRequestEvent;
import com.cs.core.kafka.topic.InfoTopics;
import com.cs.info.outbox.OutboxEvent;
import com.cs.info.outbox.OutboxEventRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * INFO 서비스 → 타 서비스로 전파되는 이벤트를 발행하는 Producer
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InfoEventProducer {

    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * 게시글 메일 발송 요청 이벤트
     */
    public void sendPostMailRequestEvent(PostMailRequestEvent event) {
        saveOutbox(InfoTopics.POST_MAIL_REQUEST, event.getPostId().toString(), event);
    }

    /**
     * Outbox에 이벤트 저장 (공통 로직)
     */
    private void saveOutbox(String type, String aggregateId, Object evt) {
        try {
            String payload = objectMapper.writeValueAsString(evt);
            OutboxEvent out = new OutboxEvent(
                    UUID.randomUUID(),
                    "INFO",                     // aggregateType
                    aggregateId,
                    type,
                    payload,
                    OutboxEvent.St.READY,
                    Instant.now()
            );
            outboxRepository.save(out);
            log.info("=================================================================================");
            log.info("[Kafka-Produce Outbox-Record][INFO] type={} aggId={}", type, aggregateId);
            log.info("=================================================================================");
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event", e);
        }
    }
}

