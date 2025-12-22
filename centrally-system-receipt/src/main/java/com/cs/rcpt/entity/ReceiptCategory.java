package com.cs.rcpt.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.Comment;

import com.cs.core.entity.AuditTimeEntity;
import com.cs.core.entity.SoftDeleteEntity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 영수증 카테고리(목적별 한도) 마스터
 */
@Entity
@Table(name = "receipt_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE receipt_category SET enabled = false, deleted_at = now() WHERE category_id = ?")
public class ReceiptCategory extends SoftDeleteEntity {

    /** PK */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id", nullable = false)
    @Comment("카테고리 ID")
    private Integer categoryId;

    /** 카테고리명 (ex. 야근저녁, 회식비) */
    @Column(name = "category_name", length = 100)
    @Comment("카테고리 이름")
    private String categoryName;

    /** 한도 금액(원) */
    @Column(name = "limit_price")
    @Comment("상한 금액")
    private Integer limitPrice;
    
    
}
