package com.cs.info.entity;

import com.cs.core.entity.AuditTimeEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;

import org.hibernate.annotations.Comment;

/**
 * 첨부파일 엔티티
 */
@Entity
@Table(name = "info_post_attachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class InfoPostAttachment extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    @Comment("첨부 파일 ID")
    private Long id;

    @Column(name = "file_url", length = 512)
    @Comment("파일 경로")
    private String fileUrl;

    @Column(name = "file_type")
    @Comment("파일 타입")
    private String fileType;

    @Column(name = "upload_date")
    @Comment("업로드 날짜")
    private Date uploadDate;

    @Column(name = "file_origin_name")
    @Comment("원본 이미지 이름")
    private String fileOriginName;

    @Column(name = "file_name")
    @Comment("이미지 이름")
    private String fileName;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "post_id")
    @Comment("게시글 ID")
    private InfoPost post;
}

