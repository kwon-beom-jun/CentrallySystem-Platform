package com.cs.info.kafka.consumer;

import com.cs.core.kafka.group.Group;
import com.cs.core.kafka.topic.HrmTopics;
import com.cs.core.kafka.event.hrm.DepartmentCreatedEvent;
import com.cs.core.kafka.event.hrm.DepartmentDeletedEvent;
import com.cs.core.kafka.event.hrm.DepartmentRenamedEvent;
import com.cs.core.kafka.event.hrm.TeamCreatedEvent;
import com.cs.core.kafka.event.hrm.TeamDeletedEvent;
import com.cs.core.kafka.event.hrm.TeamRenamedEvent;
import com.cs.info.service.InfoDepartmentTeamSyncService;
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
 * HRM 서비스 부서/팀 이벤트 수신해 INFO 서비스에 동기화
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HrmEventConsumer {

    private final InfoDepartmentTeamSyncService syncService;
    private final ObjectMapper objectMapper;

    /**
     * 부서/팀 변경 이벤트 수신
     */
    @KafkaListener(
        topics = {
            HrmTopics.DEPARTMENT_CREATED,
            HrmTopics.DEPARTMENT_RENAMED,
            HrmTopics.DEPARTMENT_DELETED,
            HrmTopics.TEAM_CREATED,
            HrmTopics.TEAM_RENAMED,
            HrmTopics.TEAM_DELETED
        },
        groupId = Group.INFO_GROUP,
        containerFactory = "kafkaListenerContainerFactoryInfo"
    )
    @Transactional
    public void onMessage(String message, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic, Acknowledgment ack) {
        try {
            log.info("=================================================================================");
            log.info("[Kafka-Consume][INFO] Topic: {}", topic);
            log.info("=================================================================================");

            if (HrmTopics.DEPARTMENT_CREATED.equals(topic)) {
                DepartmentCreatedEvent event = objectMapper.readValue(message, DepartmentCreatedEvent.class);
                syncService.syncDepartmentCreated(event);
            } else if (HrmTopics.DEPARTMENT_RENAMED.equals(topic)) {
                DepartmentRenamedEvent event = objectMapper.readValue(message, DepartmentRenamedEvent.class);
                syncService.syncDepartmentRenamed(event);
            } else if (HrmTopics.DEPARTMENT_DELETED.equals(topic)) {
                DepartmentDeletedEvent event = objectMapper.readValue(message, DepartmentDeletedEvent.class);
                syncService.syncDepartmentDeleted(event);
            } else if (HrmTopics.TEAM_CREATED.equals(topic)) {
                TeamCreatedEvent event = objectMapper.readValue(message, TeamCreatedEvent.class);
                syncService.syncTeamCreated(event);
            } else if (HrmTopics.TEAM_RENAMED.equals(topic)) {
                TeamRenamedEvent event = objectMapper.readValue(message, TeamRenamedEvent.class);
                syncService.syncTeamRenamed(event);
            } else if (HrmTopics.TEAM_DELETED.equals(topic)) {
                TeamDeletedEvent event = objectMapper.readValue(message, TeamDeletedEvent.class);
                syncService.syncTeamDeleted(event);
            }
            
            // 처리 완료 후 Kafka offset commit
            ack.acknowledge();
        } catch (Exception ex) {
            log.info("=================================================================================");
            log.error("[Kafka-Consume][INFO] HRM event parse error", ex);
            log.info("=================================================================================");
            // 에러 발생 시 commit 안 함 → 재시작 후 다시 처리
            throw new RuntimeException(ex);
        }
    }
}

