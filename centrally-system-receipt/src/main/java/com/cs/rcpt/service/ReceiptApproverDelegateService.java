package com.cs.rcpt.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs.rcpt.entity.ReceiptApproverDelegate;
import com.cs.rcpt.repository.ReceiptApproverDelegateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReceiptApproverDelegateService {

    private final ReceiptApproverDelegateRepository repository;

    public Optional<ReceiptApproverDelegate> getActiveDelegate(Integer principalUserId) {
        return repository.findActiveDelegate(principalUserId, LocalDateTime.now());
    }

    /**
     * 현재 나를 대리자로 지정한 원결재자 목록(여러 명 가능)
     */
    public java.util.List<ReceiptApproverDelegate> getActivePrincipalsByDelegate(Integer delegateUserId) {
        return repository.findActivePrincipalsByDelegate(delegateUserId, LocalDateTime.now());
    }

    public ReceiptApproverDelegate createDelegate(ReceiptApproverDelegate mapping) {
        // 활성 매핑이 이미 있으면 업데이트(업서트)
        return repository.findActiveDelegate(mapping.getPrincipalUserId(), LocalDateTime.now())
                .map(exist -> {
                    exist.setDelegateUserId(mapping.getDelegateUserId());
                    exist.setDelegateName(mapping.getDelegateName());
                    exist.setDelegateEmail(mapping.getDelegateEmail());
                    exist.setDelegateDepartment(mapping.getDelegateDepartment());
                    exist.setDelegateTeam(mapping.getDelegateTeam());
                    exist.setEffectiveFrom(mapping.getEffectiveFrom());
                    exist.setEffectiveTo(mapping.getEffectiveTo());
                    exist.setEnabled(true);
                    return repository.save(exist);
                })
                .orElseGet(() -> repository.save(mapping));
    }

    public void deleteDelegate(Long id) {
        repository.deleteById(id);
    }

    /**
     * 사용자 비활성화 시: 해당 사용자가 원결재자/대리결재자 어느 쪽으로든 얽힌 모든 대리 매핑 소프트 삭제
     */
    @Transactional
    public int softDeleteAllMappingsByUser(Integer userId) {
        return repository.softDeleteAllByUserId(userId);
    }
}



