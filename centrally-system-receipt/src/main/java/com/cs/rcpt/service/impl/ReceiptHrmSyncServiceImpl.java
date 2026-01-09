package com.cs.rcpt.service.impl;

import com.cs.rcpt.enums.ReceiptProcessStatus;
import com.cs.rcpt.repository.ReceiptRepository;
import com.cs.rcpt.service.ReceiptHrmSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

/**
 * 영수증 HRM 동기화 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class ReceiptHrmSyncServiceImpl implements ReceiptHrmSyncService {

    private final ReceiptRepository receiptRepository;

    /** 동기화 대상: '신청' 단계(대기·요청) 영수증 */
    private static final List<ReceiptProcessStatus> SYNC_STATUSES =
            List.copyOf(EnumSet.of(
                ReceiptProcessStatus.WAITING,   // 대기
                ReceiptProcessStatus.REJECTED,  // 반려
                ReceiptProcessStatus.REQUEST    // 결재선 요청 중
            ));

    /* ---------------- 부서 ---------------- */

    @Override
    @Transactional
    public int renameDepartment(Integer deptId, String newName) {
        return receiptRepository.renameDepartment(deptId, newName, SYNC_STATUSES);
    }

    @Override
    @Transactional
    public int deleteDepartment(Integer deptId) {
        return receiptRepository.nullifyDepartment(deptId, SYNC_STATUSES);
    }

    /* ---------------- 팀 ---------------- */

    @Override
    @Transactional
    public int renameTeam(Integer teamId, String newName) {
        return receiptRepository.renameTeam(teamId, newName, SYNC_STATUSES);
    }

    @Override
    @Transactional
    public int deleteTeam(Integer teamId) {
        return receiptRepository.nullifyTeam(teamId, SYNC_STATUSES);
    }

    /* ---------- 사용자 부서/팀 동기화 ---------- */
    @Override
    @Transactional
    public int updateUserDeptTeam(Integer userId,
                                  Integer deptId, String deptName,
                                  Integer teamId,  String teamName) {
        return receiptRepository.updateUserDeptTeam(
                userId, deptId, deptName, teamId, teamName, SYNC_STATUSES);
    }
}

