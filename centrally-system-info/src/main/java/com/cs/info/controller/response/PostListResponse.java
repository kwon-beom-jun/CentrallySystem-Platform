package com.cs.info.controller.response;

import java.time.LocalDateTime;

import com.cs.info.enums.VisibilityScope;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 게시글 목록 조회용 Response
 */
@Getter
@Setter
@NoArgsConstructor
public class PostListResponse {
    private Long id;
    private Long boardId;
    private String title;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime creationDate;
    private Integer writerId;
    private String writerName;
    private Boolean isPrivate;
    private Integer viewCount;
    private Boolean pinned;
    private String createdBy;
    private String modifiedBy;
    private String contentPreview; // HTML 태그 제거 후 50자
    private VisibilityScope visibilityScope; // 열람 범위

    /**
     * 생성자 (JPQL 프로젝션용)
     */
    public PostListResponse(Long id, Long boardId, String title, LocalDateTime creationDate,
                       Integer writerId, String writerName, Boolean isPrivate, Integer viewCount,
                       Boolean pinned, String createdBy, String modifiedBy, String content,
                       VisibilityScope visibilityScope) {
        this.id = id;
        this.boardId = boardId;
        this.title = title;
        this.creationDate = creationDate;
        this.writerId = writerId;
        this.writerName = writerName;
        this.isPrivate = isPrivate;
        this.viewCount = viewCount;
        this.pinned = pinned;
        this.createdBy = createdBy;
        this.modifiedBy = modifiedBy;
        this.contentPreview = content; // 서비스에서 가공
        this.visibilityScope = visibilityScope;
    }
}

