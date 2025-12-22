package com.cs.hrm.controller;

import com.cs.hrm.entity.HrmEmploymentType;
import com.cs.hrm.service.HrmEmploymentTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employment-types")
public class HrmEmploymentTypeController {

    private final HrmEmploymentTypeService employmentTypeService;

    public HrmEmploymentTypeController(HrmEmploymentTypeService employmentTypeService) {
        this.employmentTypeService = employmentTypeService;
    }

    /**
     * 전체 고용 형태 정보를 조회하는 API 엔드포인트입니다.
     *
     * @return ResponseEntity로 감싼 고용 형태 목록
     */
    @GetMapping
    public ResponseEntity<List<HrmEmploymentType>> getEmploymentTypes() {
        List<HrmEmploymentType> types = employmentTypeService.getEmploymentTypes();
        return ResponseEntity.ok(types);
    }
}
