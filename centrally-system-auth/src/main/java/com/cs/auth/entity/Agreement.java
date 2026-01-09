package com.cs.auth.entity;

import com.cs.core.entity.AuditTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "agreements")
public class Agreement extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "agreement_id")
    @Comment("약관 고유 ID")
    private Long agreementId;

    @Column(nullable = false, length = 255)
    @Comment("약관 제목")
    private String title;

    @Lob
    @Column(nullable = false)
    @Comment("약관 전문")
    private String content;

    @Column(name = "is_required", nullable = false)
    @Comment("필수 약관 여부")
    private Boolean isRequired;

    @Column(name = "language_code", nullable = false, length = 10)
    @Comment("언어 코드 (ISO 639-1 기반)")
    private String languageCode;

    @Column(nullable = false, length = 20)
    @Comment("약관 버전")
    private String version;

    @Column(name = "effective_date", nullable = false)
    @Comment("약관 시행 일자")
    private LocalDate effectiveDate;
} 