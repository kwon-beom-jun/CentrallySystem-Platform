package com.cs.rcpt.service;

import com.cs.rcpt.controller.request.DefaultApproverDto;
import com.cs.rcpt.controller.request.StepOrderDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 영수증 기본 결재자 서비스 인터페이스
 */
public interface ReceiptDefaultApproverService {

    /**
     * 조회 – 전체(페이징)
     * @param pageable 페이징 정보
     * @return 기본 결재자 페이지
     */
    Page<DefaultApproverDto> getDefaultApprovers(Pageable pageable);

    /**
     * 특정 사용자가 고정 합의자인지 확인
     * @param userId 사용자 ID
     * @return 고정 합의자 여부
     */
    boolean isDefaultApprover(Long userId);

    /**
     * 기본 결재자 등록
     * @param dto 기본 결재자 DTO
     * @return 생성된 기본 결재자 DTO
     */
    DefaultApproverDto createDefaultApprovers(DefaultApproverDto dto);

    /**
     * 기본 결재자 삭제
     * @param userId 사용자 ID
     */
    void deleteDefaultApprovers(Long userId);

    /**
     * 기본 결재자 순서 변경
     * @param list 순서 목록
     */
    void reorder(List<StepOrderDto> list);
}
