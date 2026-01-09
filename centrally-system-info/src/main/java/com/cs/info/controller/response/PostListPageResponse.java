package com.cs.info.controller.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * 게시글 목록 페이징 응답
 */
@Getter
@Setter
public class PostListPageResponse {
    private List<PostListResponse> content;
    private int totalPages;
    private long totalElements;
    private int pageNumber;
    private int pageSize;
}

