package com.cs.rcpt.service.impl;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.rcpt.controller.request.ReceiptCategoryDto;
import com.cs.rcpt.entity.ReceiptCategory;
import com.cs.rcpt.repository.ReceiptCategoryRepository;
import com.cs.rcpt.service.ReceiptCategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 영수증 카테고리 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class ReceiptCategoryServiceImpl implements ReceiptCategoryService {

    private final ReceiptCategoryRepository categoryRepository;

    /** 활성화 목록 조회 + 이름순 */
    @Override
    public List<ReceiptCategory> getCategories() {
        return categoryRepository.findByEnabledTrueOrderByCategoryNameAsc();
    }

    /**
     * 비활성화된 카테고리 목록 조회
     */
    @Override
    public List<ReceiptCategory> getCategoriesWithDisabled() {
        return categoryRepository.findAllByOrderByCategoryNameAsc();
    }

    /** 등록 */
    @Override
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
    @Override
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
    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        ReceiptCategory cat = categoryRepository.findById(id).orElseThrow(
            () -> new EntityNotFoundException(GlobalExceptionHandler.CC + "카테고리(id=" + id + ") 없음"));
        // 단순히 delete만 호출하면 @SQLDelete가 동작하여 update 쿼리를 실행함
        categoryRepository.delete(cat);
    }
}

