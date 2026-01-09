package com.cs.info.entity;

import com.cs.core.entity.SoftDeleteEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;

/**
 * 댓글 엔티티
 */
@Entity
@Table(name = "info_post_comment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE info_post_comment SET enabled = false, deleted_at = now() WHERE id = ?")
public class InfoPostComment extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("댓글 ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    private InfoPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    @JsonIgnore
    private InfoPostComment parent;

    @Column(name = "writer_id", nullable = false)
    @Comment("작성자 ID")
    private Integer writerId;

    @Column(name = "writer_name")
    @Comment("작성자명")
    private String writerName;

    @Lob
    @Column(name = "content", nullable = false)
    @Comment("댓글 내용")
    private String content;

    /**
     * 부모 댓글 ID 반환 (프론트엔드용)
     */
    @Transient
    public Long getParentCommentId() {
        return parent != null ? parent.getId() : null;
    }
}

