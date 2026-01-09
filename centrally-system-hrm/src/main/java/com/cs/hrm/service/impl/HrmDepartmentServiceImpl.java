package com.cs.hrm.service.impl;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.hrm.controller.request.DepartmentCreateRequest;
import com.cs.hrm.controller.request.DepartmentUpdateRequest;
import com.cs.hrm.controller.request.TeamCreateRequest;
import com.cs.hrm.controller.request.TeamUpdateRequest;
import com.cs.hrm.controller.response.DepartmentResponse;
import com.cs.hrm.entity.HrmDepartments;
import com.cs.hrm.entity.HrmTeams;
import com.cs.hrm.kafka.producer.HrmDeptTeamEventProducer;
import com.cs.core.kafka.event.hrm.DepartmentCreatedEvent;
import com.cs.core.kafka.event.hrm.DepartmentRenamedEvent;
import com.cs.core.kafka.event.hrm.DepartmentDeletedEvent;
import com.cs.core.kafka.event.hrm.TeamCreatedEvent;
import com.cs.core.kafka.event.hrm.TeamRenamedEvent;
import com.cs.core.kafka.event.hrm.TeamDeletedEvent;
import com.cs.hrm.repository.HrmDepartmentsRepository;
import com.cs.hrm.repository.HrmTeamsRepository;
import com.cs.hrm.repository.HrmUserRepository;
import com.cs.hrm.service.HrmDepartmentService;
import com.cs.hrm.service.HrmTeamService;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HrmDepartmentServiceImpl implements HrmDepartmentService {

    private final HrmDepartmentsRepository departmentRepository;
    private final HrmTeamsRepository teamRepository;
    private final HrmTeamService teamService;
    private final HrmDeptTeamEventProducer hrmDeptTeamEventProducer;
    private final HrmUserRepository userRepository;
    private final EntityManager em;

    /**
     * [GET] 전체 부서 조회 (DTO로 변환하여 반환)
     */
    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getDepartments() {
        // 1. Fetch Join으로 N+1 문제 없이 데이터 조회
        List<HrmDepartments> departments = departmentRepository.findAllWithTeams();

        // 2. 엔티티 리스트를 DTO 리스트로 변환하여 반환
        return departments.stream()
                // 부서 이름 오름차순 정렬
                .sorted(Comparator.comparing(HrmDepartments::getDepartmentName))
                // 각 부서의 팀 목록도 팀 이름 기준으로 정렬
                .peek(dept -> {
                    if (dept.getTeams() != null) {
                        dept.getTeams().sort(Comparator.comparing(HrmTeams::getTeamName));
                    }
                })
                // HrmDepartments 엔티티를 DepartmentResponse DTO로 변환
                .map(DepartmentResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * [GET] 팀 ID로 부서 조회
     */
    @Override
    @Transactional(readOnly = true)
    public HrmDepartments getDepartmentByTeamId(int teamId) {
        return departmentRepository.findByTeamId(teamId);
    }

    /**
     * [POST] 새 부서 생성
     */
    @Override
    @Transactional
    public HrmDepartments createDepartment(DepartmentCreateRequest request) {

        /* 이름 중복 검사 */
        if (departmentRepository.existsByDepartmentNameAndEnabledTrue(request.getDepartmentName())) {
            throw new RuntimeException(GlobalExceptionHandler.CC + "부서 생성 실패\n이미 존재하는 부서입니다");
        }
        // 엔티티 생성
        HrmDepartments dept = new HrmDepartments();
        dept.setDepartmentName(request.getDepartmentName());
        
        // 필요하면 dept.setCreatedBy(...);
        // dept.setDelYn("N");
        HrmDepartments saved = departmentRepository.save(dept);
        
        // 부서 생성 이벤트 발행
        hrmDeptTeamEventProducer.sendDepartmentCreatedEvent(
            new DepartmentCreatedEvent(saved.getDepartmentId(), saved.getDepartmentName()));
        
        return saved;
    }

    /**
     * [PATCH] 부서 이름 수정
     */
    @Override
    @Transactional
    public HrmDepartments updateDepartment(Integer departmentId, DepartmentUpdateRequest request, boolean syncReceipt) {
        HrmDepartments dept = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "부서가 존재하지 않습니다. ID=" + departmentId));

        /* 새 이름이 현재 이름과 다르고, 이미 존재하면 오류 */
        if (!dept.getDepartmentName().equals(request.getDepartmentName()) &&
    		departmentRepository.existsByDepartmentNameAndEnabledTrue(request.getDepartmentName())) {
            throw new RuntimeException(GlobalExceptionHandler.CC + "부서 수정 실패\n이미 존재하는 부서입니다");
        }
        
        if (syncReceipt)
            hrmDeptTeamEventProducer.sendDepartmentRenamedEvent(
                new DepartmentRenamedEvent(departmentId, request.getDepartmentName()));
        
        dept.setDepartmentName(request.getDepartmentName());
        return departmentRepository.save(dept);
    }

    /**
     * [DELETE] 부서 삭제
     */
    @Override
    @Transactional
    public void deleteDepartment(Integer departmentId, boolean syncReceipt) {
        HrmDepartments dept = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "부서를 찾을 수 없습니다. ID=" + departmentId));

        // 부서 삭제 전, 부서에 속한 팀을 참조하던 사용자들의 team FK 를 NULL 로 설정
        if (dept.getTeams() != null && !dept.getTeams().isEmpty()) {
            dept.getTeams().forEach(team -> {
                // 팀이 활성 상태(enabled=true)든 아니든 모두 FK 해제
                userRepository.detachTeam(team.getTeamId());
            });
            // detachTeam UPDATE 쿼리를 즉시 반영
            em.flush();
        }

        if (syncReceipt)
            hrmDeptTeamEventProducer.sendDepartmentDeletedEvent(new DepartmentDeletedEvent(departmentId));

        try {
            departmentRepository.delete(dept);
        } catch (Exception e) {
            new RuntimeException(GlobalExceptionHandler.CC + "부서 삭제 실패: " + e.getMessage());
        }
    }

    // ================== 팀 관련 로직 ==================

    /**
     * [POST] 특정 부서에 팀 추가
     */
    @Override
    @Transactional
    public void createTeam(Integer departmentId, TeamCreateRequest request) {
        HrmDepartments dept = departmentRepository.findById(departmentId)
            .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "부서를 찾을 수 없습니다. ID=" + departmentId));

        /* 동일 부서 내 팀 이름 중복 검사 */
        if (teamRepository.existsByTeamNameAndDepartment_DepartmentIdAndEnabledTrue(request.getTeamName(), departmentId)) {
            throw new RuntimeException(GlobalExceptionHandler.CC + "팀 생성 실패\n이미 존재하는 팀입니다");
        }
        
        HrmTeams team = new HrmTeams();
        team.setTeamName(request.getTeamName());
        team.setDepartment(dept); // 양방향 관계인 경우 dept.getTeams().add(team)도 가능
    	HrmTeams saved = teamRepository.save(team);
    	
    	// 팀 생성 이벤트 발행
    	hrmDeptTeamEventProducer.sendTeamCreatedEvent(
    	    new TeamCreatedEvent(saved.getTeamId(), saved.getTeamName(), departmentId, dept.getDepartmentName()));
    }

    /**
     * [PATCH] 특정 부서의 특정 팀 수정
     */
    @Override
    @Transactional
    public void updateTeam(Integer departmentId, Integer teamId, TeamUpdateRequest request, boolean syncReceipt) {
        // 팀을 찾아서
        HrmTeams team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "팀이 존재하지 않습니다. teamId=" + teamId));
        // 팀이 해당 부서에 속하는지 검사
        if (!team.getDepartment().getDepartmentId().equals(departmentId)) {
            throw new RuntimeException("이 팀은 해당 부서에 속하지 않습니다.");
        }

        /* 같은 부서에서 '새 이름'이 이미 쓰이고 있나? (자기 자신 제외) */
        boolean duplicated = 
        		teamRepository.existsByTeamNameAndDepartment_DepartmentIdAndEnabledTrue(request.getTeamName(), departmentId)
        		&& !team.getTeamName().equals(request.getTeamName());

        if (duplicated) {
            throw new RuntimeException(GlobalExceptionHandler.CC +  "팀 수정 실패\n이미 존재하는 팀입니다");
        }
        
        if (syncReceipt)
            hrmDeptTeamEventProducer.sendTeamRenamedEvent(new TeamRenamedEvent(teamId, request.getTeamName()));
        
        team.setTeamName(request.getTeamName());
        teamRepository.save(team);
    }

    /**
     * [DELETE] 특정 부서의 특정 팀 삭제
     */
    @Override
    @Transactional
    public void deleteTeam(Integer departmentId, Integer teamId, boolean syncReceipt) {
        HrmTeams team = teamRepository.findById(teamId)
            .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "팀이 존재하지 않습니다. teamId=" + teamId));

        // 검증: team이 departmentId 부서에 속하는지
        if (!team.getDepartment().getDepartmentId().equals(departmentId)) {
            throw new RuntimeException(GlobalExceptionHandler.CC + "이 팀은 해당 부서에 속하지 않습니다.");
        }
        
        if (syncReceipt)
            hrmDeptTeamEventProducer.sendTeamDeletedEvent(new TeamDeletedEvent(teamId));
        
        try {
        	teamService.deleteTeam(teamId);
		} catch (Exception e) {
			throw new RuntimeException(GlobalExceptionHandler.CC + "팀 삭제 실패: " + e.getMessage());
		}
    }
}

