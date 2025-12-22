package com.cs.rcpt.service;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.rcpt.controller.request.ReceiptDeptTeamUpdate;
import com.cs.rcpt.controller.request.ReceiptDecisionRequest;
import com.cs.rcpt.entity.Receipt;
import com.cs.rcpt.entity.ReceiptApprovalLine;
import com.cs.rcpt.enums.ReceiptProcessStatus;
import com.cs.rcpt.repository.ReceiptApprovalLineRepository;
import com.cs.rcpt.repository.ReceiptRepository;
import com.cs.rcpt.entity.ReceiptApproverDelegate;

import lombok.RequiredArgsConstructor;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReceiptRequestService {

    /* ── 의존성 ─────────────────────────────── */
    private final ReceiptRepository         	receiptRepository;
    private final ReceiptApprovalLineRepository approvalLineRepository;
    private final ReceiptMailHelper            mailHelper;
    private final ReceiptApproverDelegateService delegateService;

    /** -------------------------------------------------
     * [단건] 신청자가 자신의 영수증을 ‘REQUEST’ 상태로 전환
     *  ─ WAITING → REQUEST  |  REJECTED → REQUEST(재신청)
     *  ─ 결재선에 ‘결재(approvalRole=1)’가 최소 1명 있어야 함
     *  @param receiptId  영수증 PK
     *  @param userId     로그인 사용자 PK (= 작성자)
     * -------------------------------------------------- */
    @Transactional
    public void requestReceipt(Integer receiptId, Integer userId, ReceiptDeptTeamUpdate requestDto) {
        // 포트폴리오용 - 구현 제거됨
        // 기능: 영수증을 'REQUEST' 상태로 전환하는 결재 신청 처리
        // - 결재선 유효성 검증 (최소 1명의 결재자 존재 확인)
        // - 본인 확인 (userId 일치 여부)
        // - 반려 상태일 경우 결재선 초기화
        // - 부서/팀 정보 업데이트
        // - 영수증 상태 및 결재 단계 설정
        // - 첫 번째 결재자 메일 알림 (주석 처리됨)
        throw new RuntimeException(GlobalExceptionHandler.CC + "포트폴리오용으로 구현이 제거되었습니다.");
    }

    
    /** --------------------------------------------------
     * [단건] (한 신청자) 다건 영수증 승인/반려 저장
     *  ─ ReceiptDecisionRequest 리스트 기반
     *  ─ currentApprovalStep 자동 이동 & 상태 전환
     *  @param userId     신청자 PK
     *  @param decisions  결재 결과 목록
     * -------------------------------------------------- */
    @Transactional
    public void saveReceiptDecisions(Integer userId,
                                     List<ReceiptDecisionRequest> decisions) {
        // 포트폴리오용 - 구현 제거됨
        // 기능: 영수증 승인/반려 처리 (다건 일괄 처리)
        // - 현재 결재선 찾기 (대리결재 포함하여 매칭)
        // - 대리결재 매핑 처리
        // - 승인 처리: 다음 단계로 이동 또는 최종 승인
        // - 반려 처리: 반려 사유 기록 및 상태 변경
        // - 결재선 단계별 자동 진행 로직
        // - 메일 알림 발송
        throw new RuntimeException(GlobalExceptionHandler.CC + "포트폴리오용으로 구현이 제거되었습니다.");
    }
    
    
    /** -------------------------------------------------
     * [일괄 마감] 영수증 CLOSED 전환
     *  @param receiptIds  마감 대상 PK 목록
     * -------------------------------------------------- */
    @Transactional
    public void closeReceipts(List<Integer> receiptIds) {
        // 포트폴리오용 - 구현 제거됨
        // 기능: 영수증 일괄 마감 처리 (CLOSED 상태로 전환)
        // - 영수증 상태를 CLOSED로 변경
        // - 마감일자 기록
        if (receiptIds == null || receiptIds.isEmpty()) {
            return;
        }
        throw new RuntimeException(GlobalExceptionHandler.CC + "포트폴리오용으로 구현이 제거되었습니다.");
    }

    
    /**
     * 여러 영수증을 '반려(REJECTED)' 상태로 일괄 변경
     * @param receiptIds 반려할 영수증 ID 목록
     * @param reason 반려 사유
     */
    @Transactional
    public void rejectReceipts(
    		List<Integer> receiptIds, 
    		String reason,
            Integer changerId,
            String changerName) {
        // 포트폴리오용 - 구현 제거됨
        // 기능: 영수증 일괄 반려 처리
        // - 영수증 상태를 REJECTED로 변경
        // - 반려 사유 기록
        // - 반려 알림 메일 발송
        if (receiptIds == null || receiptIds.isEmpty()) {
            return;
        }
        throw new RuntimeException(GlobalExceptionHandler.CC + "포트폴리오용으로 구현이 제거되었습니다.");
    }


    /**
     * 사용자 비활성화에 따른 영수증 후속 처리를 수행
     * - 영수증 상태가 'REQUEST' & '해당 사용자가 승인 상태'면 결재선에서 사용자만 제거하고 재정렬합니다.
     * - 영수증 상태가 'REQUEST' & '해당 사용자가 승인 전'이라면 'REJECTED'로 변경하고 사유를 기록 및 재정렬합니다.
     * - 영수증 상태가 'REJECTED' 또는 'WAITING'이면 결재선에서 사용자만 제거하고 재정렬합니다.
     */
    @Transactional
    public void rejectAndRemoveApprover(
            List<Integer> receiptIds,
            Integer userToRemoveId,
            String reason,
            Integer changerId,
            String changerName) {

        if (receiptIds == null || receiptIds.isEmpty() || userToRemoveId == null) {
            return;
        }

        List<Receipt> receiptsToProcess = receiptRepository.findAllById(receiptIds);
        String changerTag = "[" + changerName + "(" + changerId + ")] ";

        for (Receipt receipt : receiptsToProcess) {
            // 처리에 앞서, 해당 영수증의 결재선에 제거할 사용자가 있는지 먼저 확인합니다.
            Optional<ReceiptApprovalLine> lineToRemoveOpt = receipt.getApprovalLines().stream()
                .filter(line -> line.getApproverUserId().equals(userToRemoveId))
                .findFirst();

            // 결재선에 해당 사용자가 없으면 다음 영수증으로 넘어갑니다.
            if (lineToRemoveOpt.isEmpty()) {
                continue;
            }

            // 영수증 상태에 따라 분기 처리
            switch (receipt.getStatus()) {
                case REQUEST -> {
                    ReceiptApprovalLine lineToRemove = lineToRemoveOpt.get();
                    // [규칙 1-1] 신청 상태이지만, 해당 사용자가 이미 승인한 경우
                    if (lineToRemove.getApprovedAt() != null) {
                    	// 아무 작업도 수행하지 않고 현재 상태를 그대로 유지
                    	// 이 영수증에 대한 처리를 건너뜀
                    	// 해당 영수증 반려 후 재신청시 신청할때 유저 목록 재확인
//                        performRemovalAndReordering(receipt, userToRemoveId);
                    } 
                    // [규칙 1-2] 신청 상태이고, 해당 사용자가 아직 승인하지 않은 경우
                    else {
                        receipt.setStatus(ReceiptProcessStatus.REJECTED);
                        receipt.setLastRejectionDate(Date.valueOf(LocalDate.now()));
                        
                        performRemovalAndReordering(receipt, userToRemoveId);
                        
                        // 남은 결재선이 있다면 첫 번째 라인에 반려 사유 기록
                        if (!receipt.getApprovalLines().isEmpty()) {
                            receipt.getApprovalLines().get(0).setRejectedReason(changerTag + reason);
                        }
                        
                        // 📧 반려 알림
                        mailHelper.notifyReject(receipt, reason);
                    }
                }
                // [규칙 2] 대기 또는 반려 상태인 경우
//                case WAITING, REJECTED -> {
//                    performRemovalAndReordering(receipt, userToRemoveId);
//                }
                // 그 외 상태(APPROVED, CLOSED)는 아무 작업도 하지 않음
                default -> {}
            }
        }
        
        receiptRepository.saveAll(receiptsToProcess);
    }
    
    /**
     * 결재선에서 특정 사용자를 제거하고, 순서를 재정렬한 후, 다음 결재 단계를 설정하는 공통 로직
     */
    private void performRemovalAndReordering(Receipt receipt, Integer userToRemoveId) {
        // 포트폴리오용 - 구현 제거됨
        // 기능: 결재선에서 사용자 제거 및 재정렬
        // - 결재선에서 특정 사용자 제거
        // - 남은 결재선의 step_order 재정렬 (1부터 순차적으로)
        // - 다음 결재 단계(currentApprovalStep) 자동 계산
        throw new RuntimeException(GlobalExceptionHandler.CC + "포트폴리오용으로 구현이 제거되었습니다.");
    }

    /** -------------------------------------------------
     * [일괄 승인] (결재자 기준) ‘내 차례’ 영수증 다건 승인
     *  ─ approverId     : 결재자 PK
     *  ─ userIds        : 신청자 PK 배열
     *  ─ start~end      : YYYY‑MM‑DD 범위
     * -------------------------------------------------- */
    @Transactional
    public void bulkApprove(
            Integer approverId,
            String  approverName,
            List<Integer> userIds,
            LocalDate start, LocalDate end,
            List<Integer> roles ) {
        // 포트폴리오용 - 구현 제거됨
        // 기능: 결재자 기준 일괄 승인 처리
        // - 조건에 맞는 '내 차례' 영수증 조회
        // - 다건 영수증 일괄 승인 처리
        throw new RuntimeException(GlobalExceptionHandler.CC + "포트폴리오용으로 구현이 제거되었습니다.");
    }

    /** 

	/** -------------------------------------------------
	 * [일괄 반려] (결재자 기준) ‘내 차례’ 영수증 다건 반려
	 *  ─ approverId / approverName
	 *  ─ userIds        : 신청자 PK 배열
	 *  ─ start~end      : YYYY‑MM‑DD 범위
	 *  ─ reason         : 반려 사유
	 * -------------------------------------------------- */
    @Transactional
    public void bulkReject(
            Integer approverId,
            String  approverName,
            List<Integer> userIds,
            LocalDate start, LocalDate end,
            String reason,
            List<Integer> roles ) {
        // 포트폴리오용 - 구현 제거됨
        // 기능: 결재자 기준 일괄 반려 처리
        // - 조건에 맞는 '내 차례' 영수증 조회
        // - 다건 영수증 일괄 반려 처리
        throw new RuntimeException(GlobalExceptionHandler.CC + "포트폴리오용으로 구현이 제거되었습니다.");
    }
    
    /* 영수증 결재시 일괄 승인(반려) 공통 변환 메서드 */
    private List<ReceiptDecisionRequest> toDecisionDtoList(
            List<Receipt> receipts,
            Integer approverId,
            String  approverName,
            int     statusCode,
            String  reason) {

        return receipts.stream().map(r -> {
            ReceiptDecisionRequest dto = new ReceiptDecisionRequest();
            dto.setReceiptId   (r.getReceiptId());
            dto.setApproverId  (approverId);
            dto.setApproverName(approverName);
            dto.setStatusCode  (statusCode);   // 2 = 승인, 3 = 반려
            dto.setRejectReason(reason);
            return dto;
        }).toList();
    }
    
    
    /**
     * [강제 변경] 관리자용 상태/결재선 리셋
     *  ─ Receipt.status               : REQUEST | WAITING | REJECTED | APPROVED | CLOSED
     *  ─ ApprovalLine.approvalStatus  : false   = 대기
     *                                   true    = 승인
     *
     *  ▸ 규칙 정리
     *  ─ REQUEST / WAITING  ➜	결재선 **전부 대기**(approvalStatus=false) 로 초기화
     *  ─ REJECTED           ➜	영수증 상태 반려로 변경 및 반려 날짜 업데이트
     *							결재선 첫번째에 ‘반려’ 표시 + “[이름(아이디)] 강제적으로 반려처리 되었습니다” 사유
     *  ─ APPROVED / CLOSED  ➜	결재선 **전부 승인**(approvalStatus=true) 처리
     */
    @Transactional
    public void forceChangeStatus(Integer receiptId,
                                  String  statusCode,
                                  Integer changerId,
                                  String changerName,
                                  ReceiptDeptTeamUpdate requestDto) {
        // 포트폴리오용 - 구현 제거됨
        // 기능: 관리자용 영수증 상태 강제 변경
        // - REQUEST: 결재자 확인 후 결재선 초기화 및 상태 변경
        // - WAITING: 결재선 초기화 및 대기 상태로 변경
        // - REJECTED: 반려 상태로 변경 및 반려 사유 기록, 메일 알림
        // - APPROVED/CLOSED: 모든 결재선 승인 처리 및 상태 변경
        // - 부서/팀 정보 업데이트
        ReceiptProcessStatus target = ReceiptProcessStatus.valueOf(statusCode.toUpperCase());
        throw new RuntimeException(GlobalExceptionHandler.CC + "포트폴리오용으로 구현이 제거되었습니다.");
    }
}
