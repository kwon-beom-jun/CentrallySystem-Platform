package com.cs.info.dto;

import java.util.List;

import lombok.Data;

/**
 * 게시글 목록 페이징 응답 DTO
 */
@Data
public class InfoPostListPage {
    private List<InfoPostListDto> content;
    private int totalPages;
    private long totalElements;
    private int pageNumber;
    private int pageSize;
}

