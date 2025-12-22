package com.cs.rcpt.entity;

import com.cs.core.entity.AuditTimeEntity;
import com.cs.core.entity.SoftDeleteEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "receipt_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE receipt_attachments SET enabled = false, deleted_at = now() WHERE attachment_id = ?")
public class ReceiptAttachments extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id", nullable = false)
    @Comment("첨부 파일 ID")
    private Integer attachmentId;

    @Column(name = "file_url")
    @Comment("파일 경로")
    private String fileUrl;

    @Column(name = "file_type")
    @Comment("파일 타입")
    private String fileType;

    @Column(name = "upload_date")
    @Comment("업로드 날짜")
    private Date uploadDate;

    @Column(name = "file_origin_name")
    @Comment("영수증 원본 이미지 이름")
    private String fileOriginName;

    @Column(name = "file_name")
    @Comment("영수증 이미지 이름")
    private String fileName;

    /**
     * 1:1 양방향을 원한다면,
     * Receipt에서 @OneToOne(mappedBy="attachment")가 아닌
     * 여기서는 mappedBy를 사용하지 않고, 
     * 아래처럼 반대편 참조 필드를 둘 수 있습니다.
     *
     * (선택) 양방향이 필요 없다면 이 필드를 제거해도 무방.
     */
    @OneToOne(mappedBy = "attachment")
    @JsonBackReference("receipt_attachment")
    private Receipt receipt;
}
