package com.cs.info.service.impl;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.info.controller.request.ScheduleCreateRequest;
import com.cs.info.controller.request.ScheduleUpdateRequest;
import com.cs.info.entity.InfoSchedule;
import com.cs.info.entity.InfoScheduleNotification;
import com.cs.info.entity.InfoScheduleType;
import com.cs.info.repository.InfoScheduleNotificationRepository;
import com.cs.info.repository.InfoScheduleRepository;
import com.cs.info.repository.InfoScheduleTypeRepository;
import com.cs.info.service.InfoScheduleNotificationService;
import com.cs.info.service.InfoScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 일정 서비스 구현체
 */
@Service
@Slf4j
public class InfoScheduleServiceImpl implements InfoScheduleService {

    @Autowired
    private InfoScheduleRepository infoScheduleRepository;

    @Autowired
    private InfoScheduleTypeRepository infoScheduleTypeRepository;

    @Autowired
    private InfoScheduleNotificationService notificationService;

    @Autowired
    private InfoScheduleNotificationRepository notificationRepository;

    /**
     * 일정 목록 조회 (기간별)
     */
    @Override
    @Transactional(readOnly = true)
    public List<InfoSchedule> getSchedules(LocalDate startDate, LocalDate endDate, Integer departmentId, Integer assigneeId) {
        if (departmentId != null) {
            return infoScheduleRepository.findByDepartmentAndDateRange(departmentId, startDate, endDate);
        } else if (assigneeId != null) {
            return infoScheduleRepository.findByAssigneeAndDateRange(assigneeId, startDate, endDate);
        } else {
            return infoScheduleRepository.findByDateRange(startDate, endDate);
        }
    }

    /**
     * 일정 단건 조회
     */
    @Override
    @Transactional(readOnly = true)
    public InfoSchedule getSchedule(Long scheduleId) {
        return infoScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "일정을 찾을 수 없습니다: " + scheduleId));
    }

    /**
     * 일정 생성
     */
    @Override
    @Transactional
    public InfoSchedule createSchedule(ScheduleCreateRequest request, Integer creatorId, String creatorName) {
        // 포트폴리오용: 기능 비활성화
        throw new RuntimeException("포트폴리오용: 일정 생성 기능이 비활성화되어 있습니다.");
    }

    /**
     * 일정 수정 (권한 검증 필요)
     */
    @Override
    @Transactional
    public InfoSchedule updateSchedule(Long scheduleId, ScheduleUpdateRequest request, Integer editorUserId, boolean isManager) {
        InfoSchedule existingSchedule = infoScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "일정을 찾을 수 없습니다: " + scheduleId));
        
        // 권한 검증: 작성자 또는 MANAGER/ADMIN만 수정 가능
        if (!existingSchedule.getCreatorId().equals(editorUserId) && !isManager) {
            throw new IllegalArgumentException(GlobalExceptionHandler.CC + "일정을 수정할 권한이 없습니다.");
        }

        // 일정 유형 설정
        String scheduleTypeCode = request.getScheduleType();
        if (scheduleTypeCode != null && !scheduleTypeCode.isEmpty()) {
            InfoScheduleType scheduleType = infoScheduleTypeRepository.findByCodeAndEnabledTrueAndIsActiveTrue(scheduleTypeCode)
                    .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "일정 유형을 찾을 수 없습니다: " + scheduleTypeCode));
            existingSchedule.setScheduleType(scheduleType);
        }
        
        // 업데이트 가능한 필드만 수정
        existingSchedule.setTitle(request.getTitle());
        existingSchedule.setStartDate(request.getStartDate());
        existingSchedule.setEndDate(request.getEndDate());
        existingSchedule.setStartTime(request.getStartTime());
        existingSchedule.setEndTime(request.getEndTime());
        existingSchedule.setAssigneeId(request.getAssigneeId());
        existingSchedule.setAssigneeName(request.getAssigneeName());
        existingSchedule.setAssigneeEmail(request.getAssigneeEmail());
        existingSchedule.setDepartmentId(request.getDepartmentId());
        existingSchedule.setDepartmentName(request.getDepartmentName());
        existingSchedule.setDescription(request.getDescription());
        existingSchedule.setColor(request.getColor());
        existingSchedule.setNotificationTimings(request.getNotificationTimings());

        return infoScheduleRepository.save(existingSchedule);
    }

    /**
     * 일정 삭제 (소프트 딜리트, 권한 검증 필요)
     */
    @Override
    @Transactional
    public void deleteSchedule(Long scheduleId, Integer userId, boolean isManager) {
        InfoSchedule schedule = infoScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "일정을 찾을 수 없습니다: " + scheduleId));
        
        // 권한 검증: 작성자 또는 MANAGER/ADMIN만 삭제 가능
        if (!schedule.getCreatorId().equals(userId) && !isManager) {
            throw new IllegalArgumentException(GlobalExceptionHandler.CC + "일정을 삭제할 권한이 없습니다.");
        }

        infoScheduleRepository.delete(schedule);
    }
}

