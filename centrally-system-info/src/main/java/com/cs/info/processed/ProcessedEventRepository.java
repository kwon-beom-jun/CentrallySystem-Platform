package com.cs.info.processed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * 처리 완료된 이벤트 레포지토리
 */
@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
    /**
     * 이벤트 ID로 처리 여부 확인
     */
    boolean existsByEventId(UUID eventId);
}

