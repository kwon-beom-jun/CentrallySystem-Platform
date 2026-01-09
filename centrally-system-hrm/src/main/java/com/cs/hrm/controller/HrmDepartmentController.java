package com.cs.hrm.controller;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.hrm.controller.request.DepartmentCreateRequest;
import com.cs.hrm.controller.request.DepartmentUpdateRequest;
import com.cs.hrm.controller.request.TeamCreateRequest;
import com.cs.hrm.controller.request.TeamUpdateRequest;
import com.cs.hrm.controller.response.DepartmentResponse;
import com.cs.hrm.entity.HrmDepartments;
import com.cs.hrm.service.HrmDepartmentService;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class HrmDepartmentController {

    private final HrmDepartmentService departmentService;

    /**
     * [GET] 전체 부서 조회 (DTO로 응답)
     */
    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getDepartments() {
        List<DepartmentResponse> departments = departmentService.getDepartments();
        return ResponseEntity.ok(departments);
    }

    /**
     * [GET] 팀 ID로 부서 조회
     */
    @GetMapping("/{teamId}")
    public ResponseEntity<HrmDepartments> getDepartmentByTeamId(@PathVariable("teamId") Integer teamId) {
        HrmDepartments departments = departmentService.getDepartmentByTeamId(teamId);
        return ResponseEntity.ok(departments);
    }

    /**
     * [POST] 새 부서 생성
     * 예) POST /departments
     */
    @PostMapping
    public ResponseEntity<?> createDepartment(@RequestBody DepartmentCreateRequest request) {
        // DepartmentCreateRequest: { "departmentName": "AI사업본부" }
    	HrmDepartments newDept = departmentService.createDepartment(request);
        return ResponseEntity.ok(newDept);
    }

    /**
     * [PATCH] 부서 이름 수정
     * 예) PATCH /departments/1
     */
    @PatchMapping("/{departmentId}")
    public ResponseEntity<?> updateDepartment(
            @PathVariable("departmentId") Integer departmentId,
            @RequestBody DepartmentUpdateRequest request,
            @RequestParam(name="syncReceipt", defaultValue = "false") boolean syncReceipt
    ) {
        // DepartmentUpdateRequest: { "departmentName": "바뀐 부서이름" }
        HrmDepartments updatedDept = departmentService.updateDepartment(departmentId, request, syncReceipt);
        return ResponseEntity.ok(updatedDept);
    }

    /**
     * [DELETE] 부서 삭제
     * 예) DELETE /departments/1
     */
    @DeleteMapping("/{departmentId}")
    public ResponseEntity<?> deleteDepartment(
    		@PathVariable("departmentId") Integer departmentId,
            @RequestParam(name="syncReceipt", defaultValue = "false") boolean syncReceipt
    ) {
        departmentService.deleteDepartment(departmentId, syncReceipt);
        return ResponseEntity.ok("부서 삭제 성공");
    }

    // ==================== 팀 관련 API ====================

    /**
     * [POST] 특정 부서에 팀 추가
     * 예) POST /departments/1/teams
     */
    @PostMapping("/{departmentId}/teams")
    public ResponseEntity<?> createTeam(
            @PathVariable("departmentId") Integer departmentId,
            @RequestBody TeamCreateRequest request
    ) {
        // TeamCreateRequest: { "teamName": "RPA팀" }
        departmentService.createTeam(departmentId, request);
        return ResponseEntity.ok("팀 생성 성공");
    }

    /**
     * [PATCH] 특정 부서의 특정 팀 수정
     * 예) PATCH /departments/1/teams/5
     */
    @PatchMapping("/{departmentId}/teams/{teamId}")
    public ResponseEntity<?> updateTeam(
            @PathVariable("departmentId") Integer departmentId,
            @PathVariable("teamId") Integer teamId,
            @RequestBody TeamUpdateRequest request,
            @RequestParam(name="syncReceipt", defaultValue = "false") boolean syncReceipt
    ) {
        // TeamUpdateRequest: { "teamName": "수정된 팀이름" }
        departmentService.updateTeam(departmentId, teamId, request, syncReceipt);
        return ResponseEntity.ok("팀 수정 성공");
    }

    /**
     * [DELETE] 특정 부서의 특정 팀 삭제
     * 예) DELETE /departments/1/teams/5
     */
    @DeleteMapping("/{departmentId}/teams/{teamId}")
    public ResponseEntity<?> deleteTeam(
            @PathVariable("departmentId") Integer departmentId,
            @PathVariable("teamId") Integer teamId,
            @RequestParam(name="syncReceipt", defaultValue = "false") boolean syncReceipt
    ) {
        departmentService.deleteTeam(departmentId, teamId, syncReceipt);
        return ResponseEntity.ok("팀 삭제 성공");
    }
}
