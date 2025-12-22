package com.cs.hrm.controller.response;

import com.cs.hrm.entity.HrmDepartments;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class DepartmentResponse {
    private final Integer departmentId;
    private final String departmentName;
    private final List<TeamResponse> teams;

    @Builder
    public DepartmentResponse(Integer departmentId, String departmentName, List<TeamResponse> teams) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.teams = teams;
    }

    // HrmDepartments 엔티티를 DepartmentResponse DTO로 변환하는 정적 팩토리 메서드
    public static DepartmentResponse from(HrmDepartments department) {
        List<TeamResponse> teamResponses = department.getTeams().stream()
                .map(TeamResponse::from) // team -> TeamResponse.from(team) 과 동일
                .collect(Collectors.toList());

        return DepartmentResponse.builder()
                .departmentId(department.getDepartmentId())
                .departmentName(department.getDepartmentName())
                .teams(teamResponses)
                .build();
    }
}