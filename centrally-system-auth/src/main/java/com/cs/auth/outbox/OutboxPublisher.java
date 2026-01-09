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
        List<OutboxEvent> batch = repository.findTop100ByStatusOrderByCreatedAtAsc(OutboxEvent.St.READY);
        for (OutboxEvent e : batch) {
            try {
                e.markAttempt();
                kafkaTemplate.send(e.getType(), e.getAggregateId(), e.getPayload()).get();
                e.markSent();
            } catch (Exception ex) {
                e.markFailed(ex.getMessage());
            }
        }
        repository.saveAll(batch);
    }
}
