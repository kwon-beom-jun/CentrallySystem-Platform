package com.cs.info.entity;

import com.cs.core.entity.SoftDeleteEntity;
import com.cs.info.enums.BoardCode;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;

/**
 * 게시판 정의 엔티티
 */
@Entity
@Table(name = "info_board_definition")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE info_board_definition SET enabled = false, deleted_at = now() WHERE id = ?")
public class InfoBoardDefinition extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("게시판 ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_code", nullable = false, unique = true)
    @Comment("게시판 코드")
    private BoardCode boardCode;

    @Column(name = "name", nullable = false)
    @Comment("게시판 이름")
    private String name;

    @Column(name = "description")
    @Comment("게시판 설명")
    private String description;

    @Column(name = "is_active")
    @Comment("게시판 활성 여부")
    @Builder.Default
    private Boolean isActive = true;
}

