package com.cs.hrm.entity;

import com.cs.core.entity.AuditTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

/**
 * 스타일 마스터 테이블
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hrm_styles",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_style_category_code",
           columnNames = {"style_category", "style_code"}))
public class HrmStyle extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "style_id")
    @Comment("스타일 ID")
    private Integer styleId;

    @Column(name = "style_category", nullable = false, length = 20)
    @Comment("스타일 카테고리 (MAIN_CARD, INFO_MOBILE)")
    private String styleCategory;

    @Column(name = "style_code", nullable = false, length = 20)
    @Comment("스타일 코드 (default, ver1, blue 등)")
    private String styleCode;

    @Column(name = "style_name", nullable = false, length = 50)
    @Comment("스타일 이름")
    private String styleName;

    @Column(name = "description", length = 200)
    @Comment("스타일 설명")
    private String description;
}

