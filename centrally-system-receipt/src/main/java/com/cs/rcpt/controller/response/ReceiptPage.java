package com.cs.rcpt.controller.response;

import com.cs.rcpt.entity.Receipt;
import lombok.*;

import java.util.List;

/**
 * 페이징 결과를 담기 위한 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptPage<T> {
	private List<T> content;
    private int totalPages;          // 전체 페이지 수
    private long totalElements;      // 전체 요소(레코드) 수
    private int pageNumber;          // 현재 페이지 번호 (0부터 시작)
    private int pageSize;            // 페이지 크기
}
