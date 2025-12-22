package com.cs.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import org.hibernate.annotations.Comment;

/**
 * 모든 Soft-Delete 엔티티의 공통 부모
 * delete() → enabled=false 로 변경
 * 모든 SELECT → enabled=true 만 반환 (Filter)
 */
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
// @SQLDelete는 개별지정
// @SQLDelete(sql =
//    "UPDATE #{#entityName} SET enabled = false, deleted_at = now() " +
//    "WHERE #{#entityName}_id = ?")
@FilterDef(
    name = "enabledFilter",
    parameters = @ParamDef(name = "isEnabled", type = Boolean.class)
)
@Filter(name = "enabledFilter", condition = "enabled = :isEnabled")
public abstract class SoftDeleteEntity extends AuditTimeEntity {

    @Column(name = "enabled", nullable = false)
    @Comment("활성 여부")
    protected Boolean enabled = true;

    @Column(name = "deleted_at")
    @Comment("삭제 일시")
    protected LocalDateTime deletedAt;
}
