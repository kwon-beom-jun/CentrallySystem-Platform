package com.cs.info.outbox;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Outbox 패턴을 위한 이벤트 엔티티
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    public enum St {
        READY, SENT, FAILED
    }

    @Id
    private UUID id;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, columnDefinition = "text")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private St status;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant lastAttemptAt;
    private Instant sentAt;

    @Column(nullable = false)
    private int attempts;

    private String errorMessage;

    @Version
    private long version;

    /**
     * 편의 생성자
     */
    public OutboxEvent(UUID id, String aggregateType, String aggregateId, String type, String payload, St status,
            Instant createdAt) {
        this(id, aggregateType, aggregateId, type, payload, status, createdAt, null, null, 0, null, 0L);
    }

    /**
     * 시도 횟수 증가
     */
    public void markAttempt() {
        this.lastAttemptAt = Instant.now();
        this.attempts++;
    }

    /**
     * 발행 성공 표시
     */
    public void markSent() {
        this.status = St.SENT;
        this.sentAt = Instant.now();
    }

    /**
     * 발행 실패 표시
     */
    public void markFailed(String msg) {
        this.status = St.FAILED;
        this.errorMessage = msg;
        this.lastAttemptAt = Instant.now();
        this.attempts++;
    }
}

