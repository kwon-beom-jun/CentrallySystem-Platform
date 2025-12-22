package com.cs.auth.outbox;

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

    @Scheduled(fixedDelay = 1000) // 1초마다 배치
    @Transactional
    public void publish() {
        // 포트폴리오용 - 구현 제거됨
        // 기능: Outbox 패턴을 통한 Kafka 이벤트 발행
        // - READY 상태의 이벤트 조회 (최대 100건)
        // - Kafka로 이벤트 발행 (동기 처리)
        // - 발행 성공 시 SENT 상태로 변경
        // - 발행 실패 시 FAILED 상태로 변경 및 에러 메시지 기록
        // - 이벤트 상태 업데이트 저장
    }
}
