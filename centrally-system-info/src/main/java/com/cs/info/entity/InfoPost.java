package com.cs.info.entity;

import java.util.ArrayList;
import java.util.List;

import com.cs.core.entity.SoftDeleteEntity;
import com.cs.info.enums.VisibilityScope;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;

/**
 * 게시글 엔티티
 * 
 * INFO Entity 연결 시 단순 ID 사용 이유
	- 게시글-보드 관계는 “탐색 편의”보다 “경량 조회·분리”가 중요
	- 실제 로직은 보드 정의에서 id만 받아오고, 권한은 visibilityScope/역할 필터로 처리
	- 댓글/첨부는 게시글 생명주기에 종속이라 엔터티 연관으로 묶음
 */
@Entity
@Table(name = "info_post")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE info_post SET enabled = false, deleted_at = now() WHERE id = ?")
public class InfoPost extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @Comment("게시글 ID")
    private Long id;

    @Column(name = "board_id", nullable = false)
    @Comment("게시판 ID")
    private Long boardId;

    @Column(name = "title", nullable = false)
    @Comment("제목")
    private String title;

    @Lob
    @Column(name = "content")
    @Comment("내용")
    private String content;

    @Column(name = "writer_id", nullable = false)
    @Comment("작성자 ID")
    private Integer writerId;

    @Column(name = "writer_name")
    @Comment("작성자명")
    private String writerName;

    @Column(name = "is_private")
    @Comment("비공개 여부")
    @Builder.Default
    private Boolean isPrivate = false;

    @Column(name = "view_count")
    @Comment("조회수")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "pinned")
    @Comment("상단 고정 여부")
    @Builder.Default
    private Boolean pinned = false;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<InfoPostAttachment> attachments = new ArrayList<>();

    /* 열람 범위: 전체/USER이상/MANAGER이상 */
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility_scope", nullable = true)
    @Comment("열람 범위 (ALL/USER_ABOVE/MANAGER_ABOVE)")
    @Builder.Default
    private VisibilityScope visibilityScope = com.cs.info.enums.VisibilityScope.ALL;

    /* ===== 메일 발송 관련 (요청 전용, DB 미매핑) ===== */
    @Transient
    private Boolean sendMail; // 메일 발송 여부

    @Transient
    private Boolean mailToAll; // 전체 발송 여부

    @Transient
    private List<Integer> departmentIds; // 부서 타겟

    @Transient
    private List<Integer> teamIds; // 팀 타겟

    @Transient
    private Boolean mailIncludeGuest; // GUEST 포함 여부

    @Transient
    private Boolean mailIncludeUser; // USER 포함 여부

    @Transient
    private Boolean mailIncludeManager; // MANAGER 포함 여부

    @Transient
    private Boolean mailIncludeAdmin; // ADMIN 포함 여부
}

