package com.cs.core.domain;

/**
 * Kafka 기반 Saga/Outbox 프로세스에서 공통으로 사용하는 상태 값.
 */
public enum ProcessingStatus {
    PENDING,
    ACTIVE,
    FAILED
}
