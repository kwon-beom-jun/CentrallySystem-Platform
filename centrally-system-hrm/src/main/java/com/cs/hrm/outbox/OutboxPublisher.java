package com.cs.hrm.outbox;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxPublisher {

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxEventRepository repository, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publish() {
        // 포트폴리오용 - 구현 제거됨
        // 기능: Outbox 패턴을 통한 Kafka 이벤트 발행
        // - READY 상태의 이벤트 조회 및 Kafka 발행 처리
    }
}
