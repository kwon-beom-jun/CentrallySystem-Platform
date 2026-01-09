package com.cs.info.processed;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * 처리 완료된 이벤트 기록 (중복 수신 방지)
 */
@Entity
@Table(name = "processed_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEvent {
    @Id
    private UUID eventId;

    @Column(nullable = false)
    private Instant processedAt;
}

