package com.cs.auth.kafka.producer;

import com.cs.auth.outbox.OutboxEvent;
import com.cs.auth.outbox.OutboxEventRepository;
import com.cs.core.kafka.topic.AuthTopics;
import com.cs.auth.controller.request.AuthUserPermissionPatchRequest;
import com.cs.core.vo.event.AuthPermissionEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthPermissionEventProducer {

    private final OutboxEventRepository repo;
    private final ObjectMapper mapper;

    private void save(String topic, AuthUserPermissionPatchRequest r, String type) {
        try {
            String payload = mapper.writeValueAsString(new AuthPermissionEvent(
                    r.getUserId(), r.getServiceName(), r.getRoleName(), type));
            repo.save(new OutboxEvent(
                    UUID.randomUUID(),
                    "Permission",
                    r.getUserId().toString(),
                    topic,
                    payload,
                    OutboxEvent.St.READY,
                    Instant.now()));
            log.info("=================================================================================");
            log.info("[Kafka-Produce Outbox-Record][AUTH] {} : userId={}", topic, r.getUserId());
            log.info("=================================================================================");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void created(AuthUserPermissionPatchRequest r)  { save(AuthTopics.PERMISSION_CREATED , r, "created"); }
    public void updated(AuthUserPermissionPatchRequest r)  { save(AuthTopics.PERMISSION_UPDATED , r, "updated"); }
    public void deleted(AuthUserPermissionPatchRequest r)  { save(AuthTopics.PERMISSION_DELETED , r, "deleted"); }
}