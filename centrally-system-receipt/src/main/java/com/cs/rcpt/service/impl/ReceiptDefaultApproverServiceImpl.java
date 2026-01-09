package com.cs.rcpt.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.cs.rcpt.controller.request.DefaultApproverDto;
import com.cs.rcpt.controller.request.StepOrderDto;
import com.cs.rcpt.entity.ReceiptDefaultApprover;
import com.cs.rcpt.repository.ReceiptDefaultApproverRepository;
import com.cs.rcpt.service.ReceiptDefaultApproverService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * 영수증 기본 결재자 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class ReceiptDefaultApproverServiceImpl implements ReceiptDefaultApproverService {

    private final ReceiptDefaultApproverRepository repo;

    /* 조회 – 전체(페이징) */
    @Override
    @Transactional
    public Page<DefaultApproverDto> getDefaultApprovers(Pageable pageable) {

        // 정렬 파라미터가 이미 들어왔으면 그대로,
        // 없으면 stepNo 오름차순 정렬을 붙여준다.
        Pageable sorted = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by("stepNo").ascending()   // ✅
                  );

        return repo.findAll(sorted)
                   .map(this::toDto);
    }
    
    /* 특정 사용자가 고정 합의자인지 확인 */
    @Override
    @Transactional
    public boolean isDefaultApprover(Long userId) {
        return repo.existsById(userId);
    }

    /* 등록 */
    @Override
    @Transactional
    public DefaultApproverDto createDefaultApprovers(DefaultApproverDto dto) {

        if (repo.existsById(dto.getUserId()))
            throw new IllegalStateException("이미 등록된 사용자입니다.");
        if (repo.existsByEmail(dto.getEmail()))
            throw new IllegalStateException("이미 등록된 기본 합의자입니다.");

        /* 새 항목은 '맨 뒤'에 붙인다 */
        ReceiptDefaultApprover entity = toEntity(dto);
        entity.setStepNo(repo.findMaxStepNo() + 1);

        ReceiptDefaultApprover saved = repo.save(entity);

        /* 혹시 중간 삽입 등으로 틈이 생겼을 경우를 대비해 재정렬 */
        resequence();

        return toDto(saved);
    }

    /* 삭제 */
    @Override
    @Transactional
    public void deleteDefaultApprovers(Long userId) {
        repo.deleteById(userId);
        resequence();                     // 빈 번호를 메워 준다
    }
    
    /* 순서 변경 */
    @Override
    @Transactional
    public void reorder(List<StepOrderDto> list) {

        // ① 전달받은 리스트를 Map<userId , stepNo> 로 변환
        Map<Long,Integer> orderMap = list.stream()
                .collect(Collectors.toMap(StepOrderDto::getUserId, StepOrderDto::getStepNo));

        // ② 해당 id 들만 한꺼번에 가져옴
        List<ReceiptDefaultApprover> entities =
                repo.findAllById(orderMap.keySet());

        // ③ stepNo 갱신
        entities.forEach(e -> e.setStepNo(orderMap.get(e.getUserId())));

        // ④ saveAll 로 일괄 저장
        repo.saveAll(entities);
    }

    /* ===== 매핑 Util ===== */
    private DefaultApproverDto toDto(ReceiptDefaultApprover e) {
        DefaultApproverDto d = new DefaultApproverDto();
        d.setUserId(e.getUserId());
        d.setUserName(e.getUserName());
        d.setEmail(e.getEmail());
        d.setDepartment(e.getDepartment());
        d.setTeam(e.getTeam());
        return d;
    }

    private ReceiptDefaultApprover toEntity(DefaultApproverDto d) {
        return ReceiptDefaultApprover.builder()
                .userId    (d.getUserId())
                .userName  (d.getUserName())
                .email     (d.getEmail())
                .department(d.getDepartment())
                .team      (d.getTeam())
                .stepNo    (d.getStepNo())
                .build();
    }
    
    /* 공통 재정렬 헬퍼 : 모든 레코드를 1부터 다시 번호 매겨 저장 */
    private void resequence() {
        List<ReceiptDefaultApprover> list =
            repo.findAll(Sort.by("stepNo").ascending());

        int idx = 1;
        for (ReceiptDefaultApprover e : list) {
            e.setStepNo(idx++);
        }
        repo.saveAll(list);
    }
}

