package com.cs.info.service.impl;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.info.entity.InfoScheduleType;
import com.cs.info.repository.InfoScheduleTypeRepository;
import com.cs.info.service.InfoScheduleTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 일정 유형 서비스 구현체
 */
@Service
@Slf4j
public class InfoScheduleTypeServiceImpl implements InfoScheduleTypeService {

    @Autowired
    private InfoScheduleTypeRepository infoScheduleTypeRepository;

    /**
     * 활성화된 일정 유형 목록 조회 (캘린더 등에서 사용)
     */
    @Override
    @Transactional(readOnly = true)
    public List<InfoScheduleType> getAllActiveScheduleTypes() {
        return getActiveScheduleTypes();
    }

    /**
     * 삭제되지 않은 일정 유형 목록 조회 (관리 페이지에서 사용, isActive 필터링 없음)
     */
    @Override
    @Transactional(readOnly = true)
    public List<InfoScheduleType> getAllScheduleTypes() {
        return infoScheduleTypeRepository.findByEnabledTrueOrderByDisplayOrderAsc();
    }

    /**
     * 캘린더용 일정 유형 목록 조회 (enabled=true이고 isActive=true인 항목만)
     */
    @Override
    @Transactional(readOnly = true)
    public List<InfoScheduleType> getAllScheduleTypesForCalendar() {
        return getActiveScheduleTypes();
    }
    
    /**
     * 활성화된 일정 유형 목록 조회 (내부 공통 메서드)
     */
    private List<InfoScheduleType> getActiveScheduleTypes() {
        return infoScheduleTypeRepository.findByEnabledTrueAndIsActiveTrueOrderByDisplayOrderAsc();
    }

    /**
     * 코드로 일정 유형 조회
     */
    @Override
    @Transactional(readOnly = true)
    public InfoScheduleType getScheduleTypeByCode(String code) {
        return infoScheduleTypeRepository.findByCodeAndEnabledTrueAndIsActiveTrue(code)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "일정 유형을 찾을 수 없습니다: " + code));
    }

    /**
     * 일정 유형 단건 조회
     */
    @Override
    @Transactional(readOnly = true)
    public InfoScheduleType getScheduleType(Long scheduleTypeId) {
        return infoScheduleTypeRepository.findById(scheduleTypeId)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "일정 유형을 찾을 수 없습니다: " + scheduleTypeId));
    }

    /**
     * 일정 유형 생성
     */
    @Override
    @Transactional
    public InfoScheduleType createScheduleType(InfoScheduleType scheduleType) {
        // 코드 중복 체크 (enabled=true인 것만)
        if (infoScheduleTypeRepository.findByCodeAndEnabledTrue(scheduleType.getCode()).isPresent()) {
            throw new RuntimeException(GlobalExceptionHandler.CC + "이미 존재하는 일정 유형 코드입니다: " + scheduleType.getCode());
        }
        
        // 표시 순서 범위 검증 및 정규화 (1~999)
        int displayOrder = scheduleType.getDisplayOrder() != null ? scheduleType.getDisplayOrder() : 1;
        displayOrder = validateAndNormalizeDisplayOrder(displayOrder);
        scheduleType.setDisplayOrder(displayOrder);
        
        // 표시 순서 재정렬: 새 순서보다 크거나 같은 항목들을 뒤로 밀기
        List<InfoScheduleType> existingTypes = infoScheduleTypeRepository.findByEnabledTrueOrderByDisplayOrderAsc();
        for (InfoScheduleType existing : existingTypes) {
            if (existing.getDisplayOrder() >= displayOrder) {
                existing.setDisplayOrder(existing.getDisplayOrder() + 1);
            }
        }
        infoScheduleTypeRepository.saveAll(existingTypes);
        
        return infoScheduleTypeRepository.save(scheduleType);
    }

    /**
     * 일정 유형 수정
     */
    @Override
    @Transactional
    public InfoScheduleType updateScheduleType(Long scheduleTypeId, InfoScheduleType scheduleTypeDetails) {
        InfoScheduleType scheduleType = infoScheduleTypeRepository.findById(scheduleTypeId)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "일정 유형을 찾을 수 없습니다: " + scheduleTypeId));
        
        // 코드 중복 체크
        validateCodeUniqueness(scheduleType, scheduleTypeDetails);
        
        // 상태 및 순서 정보 확인
        boolean wasActive = scheduleType.getIsActive() != null && scheduleType.getIsActive();
        boolean willBeActive = scheduleTypeDetails.getIsActive() != null && scheduleTypeDetails.getIsActive();
        boolean isChangingToInactive = wasActive && !willBeActive;
        boolean isChangingToActive = !wasActive && willBeActive;
        boolean isActiveChanging = wasActive != willBeActive;
        
        int oldDisplayOrder = scheduleType.getDisplayOrder();
        int newDisplayOrder = scheduleTypeDetails.getDisplayOrder() != null ? scheduleTypeDetails.getDisplayOrder() : oldDisplayOrder;
        boolean isDisplayOrderChanging = oldDisplayOrder != newDisplayOrder;
        
        // 상태 변경 및 순서 처리
        // 활성 -> 비활성: displayOrder를 -1로 설정하고 재정렬
        if (isChangingToInactive) {
            // 최소 1개 활성화 유지 검증
            long activeCount = infoScheduleTypeRepository.countByEnabledTrueAndIsActiveTrue();
            if (activeCount <= 1) {
                throw new RuntimeException(GlobalExceptionHandler.CC + "최소 하나 이상의 활성화된 일정 유형이 필요합니다.");
            }
            scheduleType.setIsActive(false);
            scheduleType.setDisplayOrder(-1);
            infoScheduleTypeRepository.save(scheduleType);
            reorderActiveScheduleTypes();
            newDisplayOrder = -1;
        }
        // 비활성 -> 활성: 사용자가 보낸 displayOrder를 기준으로 밀기/당기기 후 재정렬
        else if (isChangingToActive) {
            newDisplayOrder = validateAndNormalizeDisplayOrder(newDisplayOrder);
            
            // 현재 항목을 제외한 활성 항목들 가져오기
            List<InfoScheduleType> activeTypes = infoScheduleTypeRepository.findByEnabledTrueAndIsActiveTrueOrderByDisplayOrderAsc()
                    .stream()
                    .filter(t -> t.getDisplayOrder() != null && t.getDisplayOrder() >= 0)
                    .collect(Collectors.toList());
            
            // 사용자가 지정한 순서에 맞게 기존 항목들 밀기
            for (InfoScheduleType active : activeTypes) {
                if (active.getDisplayOrder() >= newDisplayOrder) {
                    active.setDisplayOrder(active.getDisplayOrder() + 1);
                }
            }
            infoScheduleTypeRepository.saveAll(activeTypes);
            
            scheduleType.setIsActive(true);
            scheduleType.setDisplayOrder(newDisplayOrder);
            infoScheduleTypeRepository.save(scheduleType);
            reorderActiveScheduleTypes();
            newDisplayOrder = getUpdatedDisplayOrder(scheduleTypeId, scheduleType);
        }
        // 활성 상태에서 순서만 변경: 밀기/당기기 후 재정렬
        else if (!isActiveChanging && isDisplayOrderChanging && wasActive) {
            newDisplayOrder = validateAndNormalizeDisplayOrder(newDisplayOrder);
            
            // 현재 항목을 제외한 활성 항목들 가져오기
            List<InfoScheduleType> activeTypes = infoScheduleTypeRepository.findByEnabledTrueAndIsActiveTrueOrderByDisplayOrderAsc()
                    .stream()
                    .filter(t -> !t.getScheduleTypeId().equals(scheduleTypeId) && t.getDisplayOrder() != null && t.getDisplayOrder() >= 0)
                    .collect(Collectors.toList());
            
            // 새 순서에 맞게 기존 항목들 밀기/당기기
            if (newDisplayOrder < oldDisplayOrder) {
                // 앞으로 이동: 새 순서 이상이고 기존 순서 미만인 항목들을 뒤로 밀기
                for (InfoScheduleType active : activeTypes) {
                    if (active.getDisplayOrder() >= newDisplayOrder && active.getDisplayOrder() < oldDisplayOrder) {
                        active.setDisplayOrder(active.getDisplayOrder() + 1);
                    }
                }
            } else {
                // 뒤로 이동: 기존 순서 초과이고 새 순서 이하인 항목들을 앞으로 당기기
                for (InfoScheduleType active : activeTypes) {
                    if (active.getDisplayOrder() > oldDisplayOrder && active.getDisplayOrder() <= newDisplayOrder) {
                        active.setDisplayOrder(active.getDisplayOrder() - 1);
                    }
                }
            }
            
            infoScheduleTypeRepository.saveAll(activeTypes);
            scheduleType.setDisplayOrder(newDisplayOrder);
            infoScheduleTypeRepository.save(scheduleType);
            reorderActiveScheduleTypes();
            newDisplayOrder = getUpdatedDisplayOrder(scheduleTypeId, scheduleType);
        }
        
        // 필드 업데이트
        scheduleType.setCode(scheduleTypeDetails.getCode());
        scheduleType.setLabel(scheduleTypeDetails.getLabel());
        scheduleType.setColor(scheduleTypeDetails.getColor());
        scheduleType.setDisplayOrder(newDisplayOrder);
        scheduleType.setIsActive(scheduleTypeDetails.getIsActive());
        
        return infoScheduleTypeRepository.save(scheduleType);
    }
    
    /**
     * 수정 시 코드 중복 체크
     */
    private void validateCodeUniqueness(InfoScheduleType scheduleType, InfoScheduleType scheduleTypeDetails) {
        if (!scheduleType.getCode().equals(scheduleTypeDetails.getCode())) {
            if (infoScheduleTypeRepository.findByCodeAndEnabledTrue(scheduleTypeDetails.getCode()).isPresent()) {
                throw new RuntimeException(GlobalExceptionHandler.CC + "이미 존재하는 일정 유형 코드입니다: " + scheduleTypeDetails.getCode());
            }
        }
    }
    
    /**
     * 표시 순서 범위 검증 및 정규화 (1~999)
     */
    private int validateAndNormalizeDisplayOrder(int displayOrder) {
        if (displayOrder < 1) return 1;
        if (displayOrder > 999) return 999;
        return displayOrder;
    }
    
    /**
     * 재정렬 후 업데이트된 순서 조회
     */
    private int getUpdatedDisplayOrder(Long scheduleTypeId, InfoScheduleType scheduleType) {
        InfoScheduleType updated = infoScheduleTypeRepository.findById(scheduleTypeId).orElse(scheduleType);
        return updated.getDisplayOrder();
    }

    /**
     * 일정 유형 삭제 (소프트 딜리트)
     */
    @Override
    @Transactional
    public void deleteScheduleType(Long scheduleTypeId) {
        InfoScheduleType scheduleType = infoScheduleTypeRepository.findById(scheduleTypeId)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "일정 유형을 찾을 수 없습니다: " + scheduleTypeId));
        
        // 삭제 전 최소 1개 활성화 유지 검증
        if (scheduleType.getIsActive() != null && scheduleType.getIsActive()) {
            long activeCount = infoScheduleTypeRepository.countByEnabledTrueAndIsActiveTrue();
            if (activeCount <= 1) {
                throw new RuntimeException(GlobalExceptionHandler.CC + "최소 하나 이상의 활성화된 일정 유형이 필요합니다.");
            }
        }
        
        // 소프트딜리트 (@SQLDelete가 enabled = false, deleted_at = now(), display_order = -1 설정)
        infoScheduleTypeRepository.delete(scheduleType);
        
        // 삭제 후 활성 항목들을 1부터 순차적으로 재정렬
        reorderActiveScheduleTypes();
    }

    /**
     * 활성 항목들을 1부터 순차적으로 재정렬
     * enabled=true이고 isActive=true인 항목들만 재정렬
     */
    private void reorderActiveScheduleTypes() {
        // enabled=true이고 isActive=true인 모든 항목 조회 (displayOrder >= 0인 것만)
        List<InfoScheduleType> activeTypes = infoScheduleTypeRepository.findByEnabledTrueAndIsActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .filter(t -> t.getDisplayOrder() != null && t.getDisplayOrder() >= 0)
                .collect(Collectors.toList());
        
        // 1부터 순차적으로 재정렬
        int order = 1;
        for (InfoScheduleType active : activeTypes) {
            active.setDisplayOrder(order++);
        }
        infoScheduleTypeRepository.saveAll(activeTypes);
    }
}

