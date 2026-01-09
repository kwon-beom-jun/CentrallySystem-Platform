package com.cs.hrm.controller;

import com.cs.hrm.service.HrmDepartmentService;
import com.cs.hrm.service.HrmPositionsService;
import com.cs.hrm.service.HrmEmploymentTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/metadata")
public class HrmUserMetadataController {

    private final HrmDepartmentService departmentService;
    private final HrmPositionsService positionsService;
    private final HrmEmploymentTypeService employmentTypeService;

    public HrmUserMetadataController(HrmDepartmentService departmentService, 
                                 HrmPositionsService positionsService, 
                                 HrmEmploymentTypeService employmentTypeService) {
        this.departmentService = departmentService;
        this.positionsService = positionsService;
        this.employmentTypeService = employmentTypeService;
    }

    /**
     * 부서, 팀, 직책, 고용 형태 정보를 한 번에 조회하는 API
     *
     * @return 부서, 팀, 직책, 고용 형태 데이터를 포함하는 JSON 응답
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMetadatas() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("departments", departmentService.getDepartments());
        metadata.put("positions", positionsService.getPositions());
        metadata.put("employmentTypes", employmentTypeService.getEmploymentTypes());
        return ResponseEntity.ok(metadata);
    }
}
