package com.cs.rcpt.repository;

import com.cs.rcpt.entity.ReceiptCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 영수증 카테고리(한도) 저장소
 */
@Repository
public interface ReceiptCategoryRepository extends JpaRepository<ReceiptCategory, Integer> {

    /** 활성화(enabled=true)된 목록만 이름순으로 조회 */
    List<ReceiptCategory> findByEnabledTrueOrderByCategoryNameAsc();

    /** 비활성화 포함 전체 목록을 이름순으로 조회 */
    List<ReceiptCategory> findAllByOrderByCategoryNameAsc();

    /** 활성화된 카테고리 중 이름 중복 여부 확인 */
    boolean existsByCategoryNameAndEnabledTrue(String categoryName);
    
    /** 동일 ID 제외 + 활성 상태에서 이름 중복 여부 */
    boolean existsByCategoryNameAndEnabledTrueAndCategoryIdNot(String categoryName, Integer categoryId);
}
