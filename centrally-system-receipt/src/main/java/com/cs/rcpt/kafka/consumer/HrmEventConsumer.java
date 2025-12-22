package com.cs.rcpt.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import com.cs.rcpt.service.ReceiptHrmSyncService;

import com.cs.core.kafka.group.Group;
import com.cs.core.kafka.topic.HrmTopics;
import com.cs.core.kafka.event.hrm.UserDeptTeamChangedEvent;
import com.cs.core.kafka.event.hrm.DepartmentRenamedEvent;
import com.cs.core.kafka.event.hrm.DepartmentDeletedEvent;
import com.cs.core.kafka.event.hrm.TeamRenamedEvent;
import com.cs.core.kafka.event.hrm.TeamDeletedEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>HRM 서비스에서 발행한 조직/사용자 관련 이벤트를 수신해 Receipt 서비스 데이터를 동기화하는 Consumer.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HrmEventConsumer {

    private final ReceiptHrmSyncService syncService;
    private final ObjectMapper mapper;

    @KafkaListener(topics = HrmTopics.USER_DEPT_TEAM_CHANGED, groupId = Group.RECEIPT_GROUP, containerFactory = "kafkaListenerContainerFactoryReceipt")
    public void onUserDeptTeamChanged(String message, Acknowledgment ack) {
        try {
            UserDeptTeamChangedEvent event = mapper.readValue(message, UserDeptTeamChangedEvent.class);
            log.info("=================================================================================");
            log.info("[Kafka-Consume][Receipt] {} : {}", HrmTopics.USER_DEPT_TEAM_CHANGED, event);
            log.info("=================================================================================");
            syncService.updateUserDeptTeam(
                    event.getUserId(),
                    event.getDepartmentId(), event.getDepartmentName(),
                    event.getTeamId(), event.getTeamName());
            
            // 처리 완료 후 Kafka offset commit
            ack.acknowledge();
        } catch (Exception ex) {
            log.info("=================================================================================");
            log.error("[Kafka-Consume][Receipt] parse error", ex);
            log.info("=================================================================================");
            // 에러 발생 시 commit 안 함 → 재시작 후 다시 처리
            throw new RuntimeException(ex);
        }
    }

    @KafkaListener(topics = HrmTopics.DEPARTMENT_RENAMED, groupId = Group.RECEIPT_GROUP, containerFactory = "kafkaListenerContainerFactoryReceipt")
    public void onDepartmentRenamed(String message, Acknowledgment ack) {
        try {
            DepartmentRenamedEvent event = mapper.readValue(message, DepartmentRenamedEvent.class);
            log.info("=================================================================================");
            log.info("[Kafka-Consume][Receipt] {} : {}", HrmTopics.DEPARTMENT_RENAMED, event);
            log.info("=================================================================================");
            syncService.renameDepartment(event.getDepartmentId(), event.getDepartmentName());
            
            // 처리 완료 후 Kafka offset commit
            ack.acknowledge();
        } catch (Exception ex) {
            log.info("=================================================================================");
            log.error("[Kafka-Consume][Receipt] parse error", ex);
            log.info("=================================================================================");
            // 에러 발생 시 commit 안 함 → 재시작 후 다시 처리
            throw new RuntimeException(ex);
        }
    }

    @KafkaListener(topics = HrmTopics.DEPARTMENT_DELETED, groupId = Group.RECEIPT_GROUP, containerFactory = "kafkaListenerContainerFactoryReceipt")
    public void onDepartmentDeleted(String message, Acknowledgment ack) {
        try {
            DepartmentDeletedEvent event = mapper.readValue(message, DepartmentDeletedEvent.class);
            log.info("=================================================================================");
            log.info("[Kafka-Consume][Receipt] {} : {}", HrmTopics.DEPARTMENT_DELETED, event);
            log.info("=================================================================================");
            syncService.deleteDepartment(event.getDepartmentId());
            
            // 처리 완료 후 Kafka offset commit
            ack.acknowledge();
        } catch (Exception ex) {
            log.info("=================================================================================");
            log.error("[Kafka-Consume][Receipt] parse error", ex);
            log.info("=================================================================================");
            // 에러 발생 시 commit 안 함 → 재시작 후 다시 처리
            throw new RuntimeException(ex);
        }
    }

    @KafkaListener(topics = HrmTopics.TEAM_RENAMED, groupId = Group.RECEIPT_GROUP, containerFactory = "kafkaListenerContainerFactoryReceipt")
    public void onTeamRenamed(String message, Acknowledgment ack) {
        try {
            TeamRenamedEvent event = mapper.readValue(message, TeamRenamedEvent.class);
            log.info("=================================================================================");
            log.info("[Kafka-Consume][Receipt] {} : {}", HrmTopics.TEAM_RENAMED, event);
            log.info("=================================================================================");
            syncService.renameTeam(event.getTeamId(), event.getTeamName());
            
            // 처리 완료 후 Kafka offset commit
            ack.acknowledge();
        } catch (Exception ex) {
            log.info("=================================================================================");
            log.error("[Kafka-Consume][Receipt] parse error", ex);
            log.info("=================================================================================");
            // 에러 발생 시 commit 안 함 → 재시작 후 다시 처리
            throw new RuntimeException(ex);
        }
    }

    @KafkaListener(topics = HrmTopics.TEAM_DELETED, groupId = Group.RECEIPT_GROUP, containerFactory = "kafkaListenerContainerFactoryReceipt")
    public void onTeamDeleted(String message, Acknowledgment ack) {
        try {
            TeamDeletedEvent event = mapper.readValue(message, TeamDeletedEvent.class);
            log.info("=================================================================================");
            log.info("[Kafka-Consume][Receipt] {} : {}", HrmTopics.TEAM_DELETED, event);
            log.info("=================================================================================");
            syncService.deleteTeam(event.getTeamId());
            
            // 처리 완료 후 Kafka offset commit
            ack.acknowledge();
        } catch (Exception ex) {
            log.info("=================================================================================");
            log.error("[Kafka-Consume][Receipt] parse error", ex);
            log.info("=================================================================================");
            // 에러 발생 시 commit 안 함 → 재시작 후 다시 처리
            throw new RuntimeException(ex);
        }
    }
} 