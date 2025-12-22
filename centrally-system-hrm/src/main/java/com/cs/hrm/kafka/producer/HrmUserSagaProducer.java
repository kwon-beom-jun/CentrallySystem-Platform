package com.cs.hrm.kafka.producer;

import com.cs.hrm.outbox.OutboxEvent;
import com.cs.hrm.outbox.OutboxEventRepository;
import com.cs.hrm.processed.ProcessedEvent;
import com.cs.hrm.processed.ProcessedEventRepository;
import com.cs.hrm.entity.HrmUser;
import com.cs.hrm.repository.HrmUserRepository;
import com.cs.hrm.entity.HrmUserProfileImg;
import com.cs.hrm.service.HrmUserService;
import com.cs.hrm.enums.EmploymentType;
import com.cs.core.kafka.topic.HrmTopics;
import com.cs.core.vo.event.AuthUserJoinedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * 가입 Saga 처리 + 결과 이벤트 Outbox 기록 Producer.
 * (AUTH → HRM 가입 요청 → HRM 처리 후 성공/실패 이벤트 반환)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HrmUserSagaProducer {

    private final HrmUserRepository userRepo;
    private final ProcessedEventRepository processedRepo;
    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper mapper;
    private final HrmUserService hrmUserService;

    @Transactional
    public void handle(String json) throws Exception {
        AuthUserJoinedEvent evt = mapper.readValue(json, AuthUserJoinedEvent.class);
        UUID eventId = UUID.randomUUID();    // 실제로는 evt.getEventId()

        if (processedRepo.existsByEventId(eventId)) return; // 멱등성

        try {
            HrmUserProfileImg defaultImg = hrmUserService.getOrCreateDefaultImg();

            HrmUser user = HrmUser.builder()
                    .userId(evt.getUserId())
                    .email(evt.getEmail())
                    .name(evt.getName())
                    .phoneNumber(evt.getPhone())
                    .profileImg(defaultImg)
                    .employmentTypeId(EmploymentType.UNASSIGNED)
                    .build();
            userRepo.save(user);

            recordSuccess(evt.getUserId().toString());
            processedRepo.save(new ProcessedEvent(eventId, Instant.now()));
        } catch (DataIntegrityViolationException ex) {
            recordFailure(evt.getUserId().toString(), "HRM_DB_ERROR");
            throw ex;
        }
    }

    /* --------------------- internal --------------------- */
    private void recordSuccess(String aggId) {
        saveOutbox(HrmTopics.USER_CREATED_SUCCESS, aggId,
                new SimpleEvt(HrmTopics.USER_CREATED_SUCCESS, aggId));
    }
    private void recordFailure(String aggId, String reason) {
        saveOutbox(HrmTopics.USER_CREATED_FAILED, aggId,
                new SimpleEvt(HrmTopics.USER_CREATED_FAILED, aggId, reason));
    }

    private void saveOutbox(String type, String aggId, Object evt) {
        try {
            String payload = mapper.writeValueAsString(evt);
            outboxRepo.save(new OutboxEvent(
                    UUID.randomUUID(), "User", aggId, type,
                    payload, OutboxEvent.St.READY, Instant.now()));
            log.info("=================================================================================");
            log.info("[Kafka-Produce Outbox-Record][HRM-Saga] type={} aggId={}", type, aggId);
            log.info("=================================================================================");
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private record SimpleEvt(String type, String aggregateId, String... reason) {}
}