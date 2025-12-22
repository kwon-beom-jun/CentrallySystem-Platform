package com.cs.rcpt.service;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.rcpt.controller.request.ReceiptCategoryDto;
import com.cs.rcpt.entity.ReceiptCategory;
import com.cs.rcpt.repository.ReceiptCategoryRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiptCategoryService {

    private final ReceiptCategoryRepository categoryRepository;

    /** 활성화 목록 조회 + 이름순 */
    public List<ReceiptCategory> getCategories() {
        return categoryRepository.findByEnabledTrueOrderByCategoryNameAsc();
    }

    /**
     * 비활성화된 카테고리 목록 조회
     */
    public List<ReceiptCategory> getCategoriesWithDisabled() {
        return categoryRepository.findAllByOrderByCategoryNameAsc();
    }

    /** 등록 */
    @Transactional
    public ReceiptCategory createCategorys(ReceiptCategoryDto dto) {
        if (categoryRepository.existsByCategoryNameAndEnabledTrue(dto.getCategoryName()))
            throw new IllegalStateException(GlobalExceptionHandler.CC + "이미 존재하는 카테고리명입니다");

        ReceiptCategory cat = ReceiptCategory.builder()
                .categoryName(dto.getCategoryName())
                .limitPrice(dto.getLimitPrice())
                .build();
        return categoryRepository.save(cat);
    }

    /** 수정 */
    @Transactional
    public ReceiptCategory updateCategorys(Integer id, ReceiptCategoryDto dto) {
        ReceiptCategory cat = categoryRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(GlobalExceptionHandler.CC + "카테고리(id=" + id + ") 없음"));

        /* 나 자신(id) 제외한 활성 레코드 중 동일 이름 존재? */
        if (categoryRepository.existsByCategoryNameAndEnabledTrueAndCategoryIdNot(dto.getCategoryName(), id))
            throw new IllegalStateException(GlobalExceptionHandler.CC + "이미 존재하는 카테고리명입니다");

        cat.setCategoryName(dto.getCategoryName());
        cat.setLimitPrice(dto.getLimitPrice());
        return categoryRepository.save(cat);
    }

    /** 삭제 (비활성화) */
    @Transactional
    public void deleteCategory(Integer id) {
        ReceiptCategory cat = categoryRepository.findById(id).orElseThrow(
            () -> new EntityNotFoundException(GlobalExceptionHandler.CC + "카테고리(id=" + id + ") 없음"));
        // 단순히 delete만 호출하면 @SQLDelete가 동작하여 update 쿼리를 실행함
        categoryRepository.delete(cat);
    }
}
