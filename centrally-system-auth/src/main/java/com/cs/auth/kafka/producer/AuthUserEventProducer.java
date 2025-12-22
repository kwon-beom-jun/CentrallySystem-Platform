package com.cs.auth.kafka.producer;

import com.cs.auth.outbox.OutboxEvent;
import com.cs.auth.outbox.OutboxEventRepository;
import com.cs.core.kafka.topic.AuthTopics;
import com.cs.core.vo.event.AuthUserJoinedEvent;
import com.cs.core.vo.event.AuthUserEnabledChangedEvent;
import com.cs.core.vo.event.AuthUserDeletedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthUserEventProducer {

    private final OutboxEventRepository repo;
    private final ObjectMapper mapper;

    private void save(String topic, String aggregateId, Object evt){
        try{
            String payload = mapper.writeValueAsString(evt);
            repo.save(new OutboxEvent(
                    UUID.randomUUID(),
                    "User",
                    aggregateId,
                    topic,
                    payload,
                    OutboxEvent.St.READY,
                    Instant.now()));
            log.info("=================================================================================");
            log.info("[Kafka-Produce Outbox-Record][AUTH] {} : aggId={}", topic, aggregateId);
            log.info("=================================================================================");
        }catch(Exception e){
            throw new IllegalStateException(e);
        }
    }

    public void created(AuthUserJoinedEvent e){
        save(AuthTopics.USER_CREATE_CMD, e.getUserId().toString(), e);
    }

    public void enabledChanged(AuthUserEnabledChangedEvent e){
        save(AuthTopics.USER_ENABLED_CHANGED, e.getUserId().toString(), e);
    }

    public void deactivated(AuthUserDeletedEvent e){
        save(AuthTopics.USER_DEACTIVATED, e.getUserId().toString(), e);
    }
}