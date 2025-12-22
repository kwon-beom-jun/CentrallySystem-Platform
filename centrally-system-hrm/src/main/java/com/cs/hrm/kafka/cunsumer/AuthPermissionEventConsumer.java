package com.cs.hrm.kafka.cunsumer;

import com.cs.core.kafka.group.Group;
import com.cs.core.kafka.topic.AuthTopics;
import com.cs.core.vo.event.AuthPermissionEvent;
import com.cs.hrm.service.HrmPermissionSyncService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * AUTH 서비스 권한 이벤트 수신해 HRM 권한 스냅샷 동기화.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthPermissionEventConsumer {

    private final HrmPermissionSyncService syncService;

    @KafkaListener(topics = {AuthTopics.PERMISSION_CREATED, AuthTopics.PERMISSION_UPDATED, AuthTopics.PERMISSION_DELETED},
                   groupId = Group.HRM_GROUP,
                   containerFactory = "kafkaListenerContainerFactoryHrmEmployment")
    @Transactional
    public void onMessage(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, Acknowledgment ack) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            AuthPermissionEvent evt = mapper.readValue(message, AuthPermissionEvent.class);
            log.info("=================================================================================");
            log.info("[Kafka-Consume][HRM] {} : {}", topic, evt);
            log.info("=================================================================================");
            if (AuthTopics.PERMISSION_CREATED.equals(topic)) {
                syncService.createPermission(evt);
            } else if (AuthTopics.PERMISSION_UPDATED.equals(topic)) {
                syncService.updatePermission(evt);
            } else if (AuthTopics.PERMISSION_DELETED.equals(topic)) {
                syncService.deletePermission(evt);
            }
            
            // 처리 완료 후 Kafka offset commit
            ack.acknowledge();
        } catch (Exception ex) {
            log.info("=================================================================================");
            log.error("[Kafka-Consume][HRM] Permission event parse error", ex);
            log.info("=================================================================================");
            // 에러 발생 시 commit 안 함 → 재시작 후 다시 처리
            throw new RuntimeException(ex);
        }
    }
}