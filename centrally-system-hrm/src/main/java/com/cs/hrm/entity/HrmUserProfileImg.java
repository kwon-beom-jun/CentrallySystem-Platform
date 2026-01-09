package com.cs.hrm.entity;

import java.sql.Date;

import com.cs.core.entity.AuditTimeEntity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hrm_user_profile_img")
public class HrmUserProfileImg extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_img_id")
    @Comment("프로필이미지 ID")
    private Integer profileImgId;

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
    @Comment("원본 이미지 이름")
    private String fileOriginName;

    @Column(name = "file_name")
    @Comment("이미지 이름")
    private String fileName;

}
