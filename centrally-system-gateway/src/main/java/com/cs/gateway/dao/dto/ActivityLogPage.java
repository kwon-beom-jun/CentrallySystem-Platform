package com.cs.gateway.dao.dto;

import com.cs.gateway.entity.UserActivityLog;
import lombok.Data;

import java.util.List;

@Data
public class ActivityLogPage {
    private List<UserActivityLog> content; // 현재 페이지 내용
    private long totalElements;            // 전체 개수
    private int totalPages;                // 총 페이지 수
    private int pageNumber;                // 현재 페이지 (1부터 시작)
    private int pageSize;                  // 페이지 크기
}
