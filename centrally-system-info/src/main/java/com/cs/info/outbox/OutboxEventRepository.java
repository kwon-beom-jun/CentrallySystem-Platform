package com.cs.info.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Outbox 이벤트 레포지토리
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    /**
     * READY 상태 중 최대 100건을 생성 시간 순으로 조회
     */
    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxEvent.St status);
}

