package com.cs.core.vo.page;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.Getter;
import lombok.Setter;

// 페이징 관련 요청 정보를 관리
@Getter
@Setter
public class PageRequestVo {
    private int page;
    private int size;
    private String sortField;
    private String sortDirection;

    public PageRequestVo() {
        this.page = 0;
        this.size = 10;
        this.sortField = "id"; // 기본 정렬 필드
        this.sortDirection = "ASC"; // 기본 정렬 방향
    }

    public Pageable toPageable() {
        Sort.Direction direction = "ASC".equalsIgnoreCase(this.sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(this.page, this.size, Sort.by(direction, this.sortField));
    }
}

