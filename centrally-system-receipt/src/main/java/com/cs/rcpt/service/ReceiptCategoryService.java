package com.cs.rcpt.service;

import com.cs.rcpt.controller.request.ReceiptCategoryDto;
import com.cs.rcpt.entity.ReceiptCategory;

import java.util.List;

/**
 * 영수증 카테고리 서비스 인터페이스
 */
public interface ReceiptCategoryService {

    /**
     * 활성화 목록 조회 + 이름순
     * @return 활성화된 카테고리 목록
     */
    List<ReceiptCategory> getCategories();

    /**
     * 비활성화된 카테고리 목록 조회
     * @return 전체 카테고리 목록
     */
    List<ReceiptCategory> getCategoriesWithDisabled();

    /**
     * 카테고리 등록
     * @param dto 카테고리 DTO
     * @return 생성된 카테고리 엔티티
     */
    ReceiptCategory createCategorys(ReceiptCategoryDto dto);

    /**
     * 카테고리 수정
     * @param id 카테고리 ID
     * @param dto 카테고리 DTO
     * @return 수정된 카테고리 엔티티
     */
    ReceiptCategory updateCategorys(Integer id, ReceiptCategoryDto dto);

    /**
     * 카테고리 삭제 (비활성화)
     * @param id 카테고리 ID
     */
    void deleteCategory(Integer id);
}
