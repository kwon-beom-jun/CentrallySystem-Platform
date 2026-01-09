package com.cs.hrm.entity;

import com.cs.core.entity.AuditTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

/**
 * 사용자별 스타일 설정
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hrm_user_styles",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_user_style_category",
           columnNames = {"user_id", "style_category"}),
       indexes = {
           @Index(name = "idx_hrm_user_styles_user_id", columnList = "user_id"),
           @Index(name = "idx_hrm_user_styles_style_category", columnList = "style_category")
       })
public class HrmUserStyle extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_style_id")
    @Comment("사용자 스타일 ID")
    private Long userStyleId;

    @Column(name = "user_id", nullable = false)
    @Comment("사용자 ID")
    private Integer userId;

    @Column(name = "style_category", nullable = false, length = 20)
    @Comment("스타일 카테고리")
    private String styleCategory;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "style_id", nullable = false)
    @Comment("스타일 ID")
    private HrmStyle style;
}

