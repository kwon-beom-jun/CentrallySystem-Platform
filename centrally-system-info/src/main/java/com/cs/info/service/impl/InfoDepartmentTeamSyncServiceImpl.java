package com.cs.info.service.impl;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.core.kafka.event.hrm.DepartmentCreatedEvent;
import com.cs.core.kafka.event.hrm.DepartmentDeletedEvent;
import com.cs.core.kafka.event.hrm.DepartmentRenamedEvent;
import com.cs.core.kafka.event.hrm.TeamCreatedEvent;
import com.cs.core.kafka.event.hrm.TeamDeletedEvent;
import com.cs.core.kafka.event.hrm.TeamRenamedEvent;
import com.cs.info.entity.InfoDepartments;
import com.cs.info.entity.InfoTeams;
import com.cs.info.repository.InfoDepartmentsRepository;
import com.cs.info.repository.InfoTeamsRepository;
import com.cs.info.service.InfoDepartmentTeamSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * HRM 서비스의 부서/팀 변경 이벤트를 수신하여 INFO 서비스에 동기화하는 서비스 구현체
 */
@Service
@Slf4j
public class InfoDepartmentTeamSyncServiceImpl implements InfoDepartmentTeamSyncService {

    @Autowired
    private InfoDepartmentsRepository departmentsRepository;

    @Autowired
    private InfoTeamsRepository teamsRepository;

    /**
     * 부서 생성 동기화
     */
    @Override
    @Transactional
    public void syncDepartmentCreated(DepartmentCreatedEvent event) {
        // 중복 체크 (이미 존재하면 무시)
        if (departmentsRepository.existsById(event.getDepartmentId())) {
            log.warn("[INFO-Sync] 부서가 이미 존재합니다: ID={}", event.getDepartmentId());
            return;
        }
        
        InfoDepartments dept = new InfoDepartments();
        dept.setDepartmentId(event.getDepartmentId());
        dept.setDepartmentName(event.getDepartmentName());
        departmentsRepository.save(dept);
        
        log.info("[INFO-Sync] 부서 생성 완료: ID={}, 이름={}", event.getDepartmentId(), event.getDepartmentName());
    }

    /**
     * 부서명 변경 동기화
     */
    @Override
    @Transactional
    public void syncDepartmentRenamed(DepartmentRenamedEvent event) {
        InfoDepartments dept = departmentsRepository.findById(event.getDepartmentId())
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "부서를 찾을 수 없습니다: " + event.getDepartmentId()));
        
        dept.setDepartmentName(event.getDepartmentName());
        departmentsRepository.save(dept);
        
        log.info("[INFO-Sync] 부서명 변경 완료: ID={}, 새이름={}", event.getDepartmentId(), event.getDepartmentName());
    }

    /**
     * 부서 삭제 동기화 (Soft Delete)
     */
    @Override
    @Transactional
    public void syncDepartmentDeleted(DepartmentDeletedEvent event) {
        InfoDepartments dept = departmentsRepository.findById(event.getDepartmentId())
                .orElse(null);
        
        if (dept != null) {
            departmentsRepository.delete(dept);
            log.info("[INFO-Sync] 부서 삭제 완료: ID={}", event.getDepartmentId());
        }
    }

    /**
     * 팀 생성 동기화
     */
    @Override
    @Transactional
    public void syncTeamCreated(TeamCreatedEvent event) {
        // 중복 체크 (이미 존재하면 무시)
        if (teamsRepository.existsById(event.getTeamId())) {
            log.warn("[INFO-Sync] 팀이 이미 존재합니다: ID={}", event.getTeamId());
            return;
        }
        
        // 부서 조회
        InfoDepartments dept = departmentsRepository.findById(event.getDepartmentId())
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "부서를 찾을 수 없습니다: " + event.getDepartmentId()));
        
        InfoTeams team = new InfoTeams();
        team.setTeamId(event.getTeamId());
        team.setTeamName(event.getTeamName());
        team.setDepartment(dept);
        teamsRepository.save(team);
        
        log.info("[INFO-Sync] 팀 생성 완료: ID={}, 이름={}, 부서={}", event.getTeamId(), event.getTeamName(), event.getDepartmentName());
    }

    /**
     * 팀명 변경 동기화
     */
    @Override
    @Transactional
    public void syncTeamRenamed(TeamRenamedEvent event) {
        InfoTeams team = teamsRepository.findById(event.getTeamId())
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "팀을 찾을 수 없습니다: " + event.getTeamId()));
        
        team.setTeamName(event.getTeamName());
        teamsRepository.save(team);
        
        log.info("[INFO-Sync] 팀명 변경 완료: ID={}, 새이름={}", event.getTeamId(), event.getTeamName());
    }

    /**
     * 팀 삭제 동기화 (Soft Delete)
     */
    @Override
    @Transactional
    public void syncTeamDeleted(TeamDeletedEvent event) {
        InfoTeams team = teamsRepository.findById(event.getTeamId())
                .orElse(null);
        
        if (team != null) {
            teamsRepository.delete(team);
            log.info("[INFO-Sync] 팀 삭제 완료: ID={}", event.getTeamId());
        }
    }
}

