package com.cs.hrm.service.impl;

import com.cs.hrm.entity.HrmEmploymentType;
import com.cs.hrm.enums.EmploymentType;
import com.cs.hrm.repository.HrmEmploymentTypeRepository;
import com.cs.hrm.service.HrmEmploymentTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HrmEmploymentTypeServiceImpl implements HrmEmploymentTypeService {

    private final HrmEmploymentTypeRepository repository;

    /** 전체 고용 형태 정보 조회 (물리 전체) */
    @Override
    @Transactional(readOnly = true)
    public List<HrmEmploymentType> getEmploymentTypes() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public HrmEmploymentType getEmploymentType(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("고용 형태를 찾을 수 없습니다. ID=" + id));
    }

    /** ID로 enum 반환 (없으면 UNASSIGNED) */
    @Override
    @Transactional(readOnly = true)
    public EmploymentType classifyById(Integer employmentTypeId) {
        if (employmentTypeId == null) return EmploymentType.UNASSIGNED;
        for (EmploymentType t : EmploymentType.values()) {
            if (t.getId() == employmentTypeId.intValue()) return t;
        }
        return EmploymentType.UNASSIGNED;
    }

    /** 고용 형태 삭제 (물리) */
    @Override
    @Transactional
    public void deleteEmploymentType(Integer id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("고용 형태를 찾을 수 없습니다. ID=" + id);
        }
        repository.deleteById(id);
    }
}

