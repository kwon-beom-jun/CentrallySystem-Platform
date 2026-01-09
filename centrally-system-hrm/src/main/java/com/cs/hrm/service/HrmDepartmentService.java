package com.cs.hrm.service;

import com.cs.hrm.controller.request.DepartmentCreateRequest;
import com.cs.hrm.controller.request.DepartmentUpdateRequest;
import com.cs.hrm.controller.request.TeamCreateRequest;
import com.cs.hrm.controller.request.TeamUpdateRequest;
import com.cs.hrm.controller.response.DepartmentResponse;
import com.cs.hrm.entity.HrmDepartments;

import java.util.List;

/**
 * 부서 서비스 인터페이스
 */
public interface HrmDepartmentService {

    /**
     * [GET] 전체 부서 조회 (DTO로 변환하여 반환)
     * @return 부서 응답 목록
     */
    List<DepartmentResponse> getDepartments();

    /**
     * [GET] 팀 ID로 부서 조회
     * @param teamId 팀 ID
     * @return 부서 엔티티
     */
    HrmDepartments getDepartmentByTeamId(int teamId);

    /**
     * [POST] 새 부서 생성
     * @param request 부서 생성 요청
     * @return 생성된 부서 엔티티
     */
    HrmDepartments createDepartment(DepartmentCreateRequest request);

    /**
     * [PATCH] 부서 이름 수정
     * @param departmentId 부서 ID
     * @param request 부서 수정 요청
     * @param syncReceipt Receipt 동기화 여부
     * @return 수정된 부서 엔티티
     */
    HrmDepartments updateDepartment(Integer departmentId, DepartmentUpdateRequest request, boolean syncReceipt);

    /**
     * [DELETE] 부서 삭제
     * @param departmentId 부서 ID
     * @param syncReceipt Receipt 동기화 여부
     */
    void deleteDepartment(Integer departmentId, boolean syncReceipt);

    /**
     * [POST] 특정 부서에 팀 추가
     * @param departmentId 부서 ID
     * @param request 팀 생성 요청
     */
    void createTeam(Integer departmentId, TeamCreateRequest request);

    /**
     * [PATCH] 특정 부서의 특정 팀 수정
     * @param departmentId 부서 ID
     * @param teamId 팀 ID
     * @param request 팀 수정 요청
     * @param syncReceipt Receipt 동기화 여부
     */
    void updateTeam(Integer departmentId, Integer teamId, TeamUpdateRequest request, boolean syncReceipt);

    /**
     * [DELETE] 특정 부서의 특정 팀 삭제
     * @param departmentId 부서 ID
     * @param teamId 팀 ID
     * @param syncReceipt Receipt 동기화 여부
     */
    void deleteTeam(Integer departmentId, Integer teamId, boolean syncReceipt);
}
