package com.cs.hrm.kafka.cunsumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import com.cs.core.kafka.group.Group;
import com.cs.core.kafka.topic.AuthTopics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cs.core.vo.event.AuthUserDeletedEvent;
import com.cs.hrm.kafka.producer.HrmUserSagaProducer;
import com.cs.core.vo.event.AuthUserEnabledChangedEvent;
import com.cs.hrm.service.HrmUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * <p>AUTH 서비스에서 발행한 사용자 이벤트를 수신해 HRM DB에 반영하는 Consumer.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthUserEventCunsumer {

    private final HrmUserService hrmUserService;
    private final ObjectMapper objectMapper;
    private final HrmUserSagaProducer sagaProducer;

    /** 
     * SAGA 패턴 – 가입 요청 처리 (Auth → HRM)
     * 임시 사용자 승인 이벤트 수신
     */
    @KafkaListener(topics = AuthTopics.USER_CREATE_CMD,
                   groupId = Group.HRM_GROUP,
                   containerFactory = "kafkaListenerContainerFactoryHrmEmployment")
    public void consumeUserCreate(String message, Acknowledgment ack) throws Exception {
        try {
            sagaProducer.handle(message);
            // 처리 완료 후 Kafka offset commit
            ack.acknowledge();
        } catch (Exception ex) {
            // 에러 발생 시 commit 안 함 → 재시작 후 다시 처리
            throw new RuntimeException(ex);
        }
    }

    /** 
     * 사용자 비활성화(삭제) 이벤트 수신
     */
    @KafkaListener(
        topics = AuthTopics.USER_DEACTIVATED,          // 새 토픽
        groupId = Group.HRM_GROUP,
        containerFactory = "kafkaListenerContainerFactoryHrmEmployment"
    )
    public void consumeAuthUserDeleted(String message, Acknowledgment ack) {
        try {
            AuthUserDeletedEvent event = objectMapper.readValue(message, AuthUserDeletedEvent.class);
            log.info("=================================================================================");
            log.info("[Kafka-Consume][HRM] {} : {}", AuthTopics.USER_DEACTIVATED, event);
            log.info("=================================================================================");
            // Soft delete (enabled=false) 처리, Patch로 처리시 orphanRemoval 기능 적용이 안됌
//            hrmUserService.patchUser(event.getUserId(), Map.of("enabled", false), false);
            hrmUserService.deleteUser(event.getUserId());
            
            // 처리 완료 후 Kafka offset commit
            ack.acknowledge();
        } catch (Exception ex) {
            log.info("=================================================================================");
            log.error("[Kafka-Consume][HRM] Error parsing USER_DEACTIVATED", ex);
            log.info("=================================================================================");
            // 에러 발생 시 commit 안 함 → 재시작 후 다시 처리
            throw new IllegalStateException(ex);
        }
    }

    /**
     * 사용자 활성화 이벤트 수신
     */
    @KafkaListener(
        topics = AuthTopics.USER_ENABLED_CHANGED,
        groupId = Group.HRM_GROUP,
        containerFactory = "kafkaListenerContainerFactoryHrmEmployment"
    )
    public void consumeAuthUserEnabledChanged(String message, Acknowledgment ack) {
        try {
            AuthUserEnabledChangedEvent event = objectMapper.readValue(message, AuthUserEnabledChangedEvent.class);
            log.info("=================================================================================");
            log.info("[Kafka-Consume][HRM] {} : {}", AuthTopics.USER_ENABLED_CHANGED, event);
            log.info("=================================================================================");
            hrmUserService.patchUser(event.getUserId(), Map.of("enabled", event.getEnabled()), false);
            
            // 처리 완료 후 Kafka offset commit
            ack.acknowledge();
        } catch (Exception ex) {
            log.info("=================================================================================");
            log.error("[Kafka-Consume][HRM] Error parsing USER_ENABLED_CHANGED", ex);
            log.info("=================================================================================");
            // 에러 발생 시 commit 안 함 → 재시작 후 다시 처리
            throw new IllegalStateException(ex);
        }
    }
    
    
}
