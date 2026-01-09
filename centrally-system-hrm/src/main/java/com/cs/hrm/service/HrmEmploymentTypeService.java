package com.cs.hrm.service;

import com.cs.hrm.entity.HrmEmploymentType;
import com.cs.hrm.enums.EmploymentType;

import java.util.List;

/**
 * 고용 형태 서비스 인터페이스
 */
public interface HrmEmploymentTypeService {

    /**
     * 전체 고용 형태 정보 조회 (물리 전체)
     * @return 고용 형태 목록
     */
    List<HrmEmploymentType> getEmploymentTypes();

    /**
     * ID로 고용 형태 조회
     * @param id 고용 형태 ID
     * @return 고용 형태 엔티티
     */
    HrmEmploymentType getEmploymentType(Integer id);

    /**
     * ID로 enum 반환 (없으면 UNASSIGNED)
     * @param employmentTypeId 고용 형태 ID
     * @return EmploymentType enum
     */
    EmploymentType classifyById(Integer employmentTypeId);

    /**
     * 고용 형태 삭제 (물리)
     * @param id 고용 형태 ID
     */
    void deleteEmploymentType(Integer id);
}
