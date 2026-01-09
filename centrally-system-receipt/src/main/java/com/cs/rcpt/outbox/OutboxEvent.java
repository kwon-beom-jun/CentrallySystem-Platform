package com.cs.rcpt.outbox;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    public enum St { READY, SENT, FAILED }

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

    public OutboxEvent(UUID id, String aggregateType, String aggregateId, String type, String payload, St status, Instant createdAt) {
        this(id, aggregateType, aggregateId, type, payload, status, createdAt, null, null, 0, null, 0L);
    }
}


