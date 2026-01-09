package com.cs.hrm.kafka.producer;

import org.springframework.stereotype.Service;

import com.cs.core.kafka.topic.HrmTopics;
import com.cs.core.kafka.event.hrm.UserDeptTeamChangedEvent;
import com.cs.core.kafka.event.hrm.DepartmentCreatedEvent;
import com.cs.core.kafka.event.hrm.DepartmentRenamedEvent;
import com.cs.core.kafka.event.hrm.DepartmentDeletedEvent;
import com.cs.core.kafka.event.hrm.TeamCreatedEvent;
import com.cs.core.kafka.event.hrm.TeamRenamedEvent;
import com.cs.core.kafka.event.hrm.TeamDeletedEvent;
import com.cs.hrm.outbox.OutboxEvent;
import com.cs.hrm.outbox.OutboxEventRepository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>HRM 서비스 → 타 서비스(Receipt 등)로 전파되는 이벤트를 발행하는 Producer.</p>
 * 인사 도메인 객체(부서/팀/사용자) 변경 사항을 Kafka 로 전달하여 수신 측에서
 * 비동기 동기화가 가능하도록 합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HrmDeptTeamEventProducer {

    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * 사용자의 부서·팀 변경 이벤트.
     */
    public void sendUserDeptTeamChangedEvent(UserDeptTeamChangedEvent event) {
        saveOutbox(HrmTopics.USER_DEPT_TEAM_CHANGED, event.getUserId().toString(), event);
    }

    /** 부서 생성 이벤트 */
    public void sendDepartmentCreatedEvent(DepartmentCreatedEvent event) {
        saveOutbox(HrmTopics.DEPARTMENT_CREATED, event.getDepartmentId().toString(), event);
    }

    /** 부서명 변경 이벤트 */
    public void sendDepartmentRenamedEvent(DepartmentRenamedEvent event) {
        saveOutbox(HrmTopics.DEPARTMENT_RENAMED, event.getDepartmentId().toString(), event);
    }

    /** 부서 삭제 이벤트 */
    public void sendDepartmentDeletedEvent(DepartmentDeletedEvent event) {
        saveOutbox(HrmTopics.DEPARTMENT_DELETED, event.getDepartmentId().toString(), event);
    }

    /** 팀 생성 이벤트 */
    public void sendTeamCreatedEvent(TeamCreatedEvent event) {
        saveOutbox(HrmTopics.TEAM_CREATED, event.getTeamId().toString(), event);
    }

    /** 팀명 변경 이벤트 */
    public void sendTeamRenamedEvent(TeamRenamedEvent event) {
        saveOutbox(HrmTopics.TEAM_RENAMED, event.getTeamId().toString(), event);
    }

    /** 팀 삭제 이벤트 */
    public void sendTeamDeletedEvent(TeamDeletedEvent event) {
        saveOutbox(HrmTopics.TEAM_DELETED, event.getTeamId().toString(), event);
    }

    /* ------------------ 내부 공통 로직 ----------------------------- */
    private void saveOutbox(String type, String aggregateId, Object evt) {
        try {
            String payload = objectMapper.writeValueAsString(evt);
            OutboxEvent out = new OutboxEvent(
                    UUID.randomUUID(),
                    "HRM",                     // aggregateType
                    aggregateId,
                    type,
                    payload,
                    OutboxEvent.St.READY,
                    Instant.now()
            );
            outboxRepository.save(out);
            log.info("=================================================================================");
            log.info("[Kafka-Produce Outbox-Record][HRM] type={} aggId={}", type, aggregateId);
            log.info("=================================================================================");
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event", e);
        }
    }
} 