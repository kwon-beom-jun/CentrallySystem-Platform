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
        List<OutboxEvent> batch = repository.findTop100ByStatusOrderByCreatedAtAsc(OutboxEvent.St.READY);
        
        if (!batch.isEmpty()) {
            log.debug("[INFO-OutboxPublisher] Processing {} events", batch.size());
        }
        
        for (OutboxEvent e : batch) {
            try {
                e.markAttempt();
                kafkaTemplate.send(e.getType(), e.getAggregateId(), e.getPayload()).get();
                e.markSent();
                log.info("=================================================================================");
                log.info("[Kafka-Publish][INFO] type={} aggId={}", e.getType(), e.getAggregateId());
                log.info("=================================================================================");
            } catch (Exception ex) {
                e.markFailed(ex.getMessage());
                log.error("[INFO-OutboxPublisher] Failed to publish event: {}", ex.getMessage());
            }
        }
        repository.saveAll(batch);
    }
}

