package com.cs.auth.kafka.consumer;

import com.cs.auth.entity.AuthUser;
import com.cs.auth.repository.AuthUserRepository;
import com.cs.core.domain.ProcessingStatus;
import com.cs.core.kafka.group.Group;
import com.cs.core.kafka.topic.HrmTopics;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * HRM 서비스에서 가입 처리 결과(hrm.user.created / failed)를 수신해
 * AuthUser 상태(PENDING -> ACTIVE/FAILED)를 업데이트하는 Consumer.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HrmUserEventConsumer {

    private final AuthUserRepository authUserRepository;
    private final ObjectMapper objectMapper;

    /**
     * HRM 가입 Saga 응답 수신.
     */
    @KafkaListener(topics = {HrmTopics.USER_CREATED_SUCCESS, HrmTopics.USER_CREATED_FAILED}, groupId = Group.AUTH_GROUP, containerFactory = "kafkaListenerContainerFactoryAuthTeam")
    @Transactional
    public void consume(String message,
                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                        ConsumerRecord<?, ?> record,
                        Acknowledgment ack) {
        try {
            SimpleEvent evt = objectMapper.readValue(message, SimpleEvent.class);
            Integer userId = Integer.valueOf(evt.aggregateId());

            AuthUser user = authUserRepository.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

            if (HrmTopics.USER_CREATED_SUCCESS.equals(topic)) {
                user.setStatus(ProcessingStatus.ACTIVE);
            } else {
                user.setStatus(ProcessingStatus.FAILED);
            }
            log.info("=================================================================================");
            log.info("[Kafka-Consume][AUTH] {} -> userId={} status={} ", topic, userId, user.getStatus());
            log.info("=================================================================================");
            
            // 처리 완료 후 Kafka offset commit
            ack.acknowledge();
        } catch (Exception ex) {
            log.info("=================================================================================");
            log.error("[Kafka-Consume][AUTH] Error processing HRM user event", ex);
            log.info("=================================================================================");
            // 에러 발생 시 commit 안 함 → 재시작 후 다시 처리
            throw new IllegalStateException(ex);
        }
    }

    /* 내부 DTO(HRM SimpleEvent와 동일 구조) */
    private record SimpleEvent(String type, String aggregateId, String[] reason) {}
}