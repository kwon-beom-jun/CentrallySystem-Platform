package com.cs.info.outbox;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Outbox 이벤트를 주기적으로 Kafka로 발행하는 스케줄러
 */
@Component
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxEventRepository repository, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 1초마다 READY 상태의 이벤트를 Kafka로 발행
     */
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publish() {
        // 포트폴리오용 - 구현 제거됨
        // 기능: Outbox 패턴을 통한 Kafka 이벤트 발행
        // - READY 상태의 이벤트 조회 및 Kafka 발행 처리
    }
}

