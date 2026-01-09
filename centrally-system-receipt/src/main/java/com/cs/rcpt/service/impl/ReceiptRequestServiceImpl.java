package com.cs.rcpt.service.impl;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.rcpt.controller.request.ReceiptDeptTeamUpdate;
import com.cs.rcpt.controller.request.ReceiptDecisionRequest;
import com.cs.rcpt.entity.Receipt;
import com.cs.rcpt.entity.ReceiptApprovalLine;
import com.cs.rcpt.entity.ReceiptApproverDelegate;
import com.cs.rcpt.enums.ReceiptProcessStatus;
import com.cs.rcpt.repository.ReceiptApprovalLineRepository;
import com.cs.rcpt.repository.ReceiptRepository;
import com.cs.rcpt.service.ReceiptApproverDelegateService;
import com.cs.rcpt.service.ReceiptMailHelper;
import com.cs.rcpt.service.ReceiptRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 영수증 신청/결재 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class ReceiptRequestServiceImpl implements ReceiptRequestService {

    /* ── 의존성 ─────────────────────────────── */
    private final ReceiptRepository         	receiptRepository;
    private final ReceiptApprovalLineRepository approvalLineRepository;
    private final ReceiptMailHelper            mailHelper;
    private final ReceiptApproverDelegateService delegateService;

    /** -------------------------------------------------
     * [단건] 신청자가 자신의 영수증을 'REQUEST' 상태로 전환
     *  ─ WAITING → REQUEST  |  REJECTED → REQUEST(재신청)
     *  ─ 결재선에 '결재(approvalRole=1)'가 최소 1명 있어야 함
     *  @param receiptId  영수증 PK
     *  @param userId     로그인 사용자 PK (= 작성자)
     * -------------------------------------------------- */
    @Override
    @Transactional
    public void requestReceipt(Integer receiptId, Integer userId, ReceiptDeptTeamUpdate requestDto) {

        /* 영수증 조회 */
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() ->
                        new RuntimeException(GlobalExceptionHandler.CC + "해당 영수증 없음"));

        /* 결재선에 '결재' 역할(approvalRole=1)을 가진 사용자가 1명 이상 존재하는지 확인 */
        boolean hasApprover = receipt
        		.getApprovalLines().stream()
                .anyMatch(line -> Integer.valueOf(1).equals(line.getApprovalRole()));
        if (!hasApprover) {
            throw new RuntimeException(GlobalExceptionHandler.CC + "결재자가 지정되지 않았습니다.");
        }

        /* 본인 확인 */
        if (!receipt.getUserId().equals(userId)) {
            throw new RuntimeException(GlobalExceptionHandler.CC + "권한 없음");
        }

        /* 반려였다면 결재선을 '대기'로 초기화 */
        if (receipt.getStatus() == ReceiptProcessStatus.REJECTED) {
            approvalLineRepository.resetByReceiptId(receiptId);
            // 영수증 & 결재선 최신 상태 다시 로드
            receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "receipt not found after reset " + receiptId));
        }

        /* 부서 및 팀 정보 업데이트 */
        // DTO의 값이 null일 수 있으므로, 그대로 set
        receipt.setDepartmentId(requestDto.getDepartmentId());
        receipt.setDepartmentName(requestDto.getDepartmentName());
        receipt.setTeamId(requestDto.getTeamId());
        receipt.setTeamName(requestDto.getTeamName());
        // 포트폴리오용: 기능 비활성화
        throw new RuntimeException("포트폴리오용: 영수증 제출 기능이 비활성화되어 있습니다.");
    }

    
    /** --------------------------------------------------
     * [단건] (한 신청자) 다건 영수증 승인/반려 저장
     *  ─ ReceiptDecisionRequest 리스트 기반
     *  ─ currentApprovalStep 자동 이동 & 상태 전환
     *  @param userId     신청자 PK
     *  @param decisions  결재 결과 목록
     * -------------------------------------------------- */
    @Override
    @Transactional
    public void saveReceiptDecisions(Integer userId,
                                     List<ReceiptDecisionRequest> decisions) {
        // 포트폴리오용: 기능 비활성화
        throw new RuntimeException("포트폴리오용: 영수증 승인/반려 기능이 비활성화되어 있습니다.");
    }
    
    
    /** -------------------------------------------------
     * [일괄 마감] 영수증 CLOSED 전환
     *  @param receiptIds  마감 대상 PK 목록
     * -------------------------------------------------- */
    @Override
    @Transactional
    public void closeReceipts(List<Integer> receiptIds) {
        // 포트폴리오용: 기능 비활성화
        throw new RuntimeException("포트폴리오용: 영수증 마감 기능이 비활성화되어 있습니다.");
    }

    
    /**
     * 여러 영수증을 '반려(REJECTED)' 상태로 일괄 변경
     * @param receiptIds 반려할 영수증 ID 목록
     * @param reason 반려 사유
     */
    @Override
    @Transactional
    public void rejectReceipts(
    		List<Integer> receiptIds, 
    		String reason,
            Integer changerId,
            String changerName) {
        // 포트폴리오용: 기능 비활성화
        throw new RuntimeException("포트폴리오용: 영수증 일괄 반려 기능이 비활성화되어 있습니다.");
    }


    /**
     * 사용자 비활성화에 따른 영수증 후속 처리를 수행
     * - 영수증 상태가 'REQUEST' & '해당 사용자가 승인 상태'면 결재선에서 사용자만 제거하고 재정렬합니다.
     * - 영수증 상태가 'REQUEST' & '해당 사용자가 승인 전'이라면 'REJECTED'로 변경하고 사유를 기록 및 재정렬합니다.
     * - 영수증 상태가 'REJECTED' 또는 'WAITING'이면 결재선에서 사용자만 제거하고 재정렬합니다.
     */
    @Override
    @Transactional
    public void rejectAndRemoveApprover(
            List<Integer> receiptIds,
            Integer userToRemoveId,
            String reason,
            Integer changerId,
            String changerName) {
        // 포트폴리오용: 기능 비활성화
        throw new RuntimeException("포트폴리오용: 영수증 결재선 제거 기능이 비활성화되어 있습니다.");
    }
    
    /**
     * 결재선에서 특정 사용자를 제거하고, 순서를 재정렬한 후, 다음 결재 단계를 설정하는 공통 로직
     */
    private void performRemovalAndReordering(Receipt receipt, Integer userToRemoveId) {
        // 1. 결재선 목록에서 특정 사용자 제거
        receipt.getApprovalLines().removeIf(
            line -> line.getApproverUserId().equals(userToRemoveId)
        );

        // 2. 남은 결재선의 step_order 재정렬
        List<ReceiptApprovalLine> remainingLines = receipt.getApprovalLines();
        remainingLines.sort(Comparator.comparing(ReceiptApprovalLine::getStepOrder));
        for (int i = 0; i < remainingLines.size(); i++) {
            remainingLines.get(i).setStepOrder(i + 1);
        }

        // 3. 새로운 currentApprovalStep 계산 및 설정
        //    남아있는 결재선 중에서 아직 승인되지 않은(approvalStatus=false) 가장 첫번째 단계를 찾습니다.
        Optional<ReceiptApprovalLine> nextLine = remainingLines.stream()
            .filter(line -> Boolean.FALSE.equals(line.getApprovalStatus()))
            .findFirst();

        // 남은 결재선에서 처리할 다음 단계(approvalStatus=false)를 찾아 설정
        // 만약 다음 단계가 없다면(예: 마지막 미승인 결재자(비활성 및 권한 제거된 사용자)가 제거된 경우), null로 설정
        // nextLine에 다음 결재자 정보가 있으면 그 사람의 결재 순번(stepOrder)을, 없으면 null을 영수증의 currentApprovalStep에 설정
        receipt.setCurrentApprovalStep(nextLine.map(ReceiptApprovalLine::getStepOrder).orElse(null));
    }

    /** -------------------------------------------------
     * [일괄 승인] (결재자 기준) '내 차례' 영수증 다건 승인
     *  ─ approverId     : 결재자 PK
     *  ─ userIds        : 신청자 PK 배열
     *  ─ start~end      : YYYY‑MM‑DD 범위
     * -------------------------------------------------- */
    @Override
    @Transactional
    public void bulkApprove(
            Integer approverId,
            String  approverName,
            List<Integer> userIds,
            LocalDate start, LocalDate end,
            List<Integer> roles ) {

        List<Receipt> receipts = receiptRepository.findMyTurnReceipts(
                approverId, userIds,
                Date.valueOf(start), Date.valueOf(end),
                roles);

        List<ReceiptDecisionRequest> decisions =
                toDecisionDtoList(receipts, approverId, approverName, 2, null);

        // 일반 특정 사원 결재(영수증은 다건) 로직 재사용
        saveReceiptDecisions(approverId, decisions);
    }

    /** 

	/** -------------------------------------------------
	 * [일괄 반려] (결재자 기준) '내 차례' 영수증 다건 반려
	 *  ─ approverId / approverName
	 *  ─ userIds        : 신청자 PK 배열
	 *  ─ start~end      : YYYY‑MM‑DD 범위
	 *  ─ reason         : 반려 사유
	 * -------------------------------------------------- */
    @Override
    @Transactional
    public void bulkReject(
            Integer approverId,
            String  approverName,
            List<Integer> userIds,
            LocalDate start, LocalDate end,
            String reason,
            List<Integer> roles ) {

        List<Receipt> receipts = receiptRepository.findMyTurnReceipts(
                approverId, userIds,
                Date.valueOf(start), Date.valueOf(end),
                roles);

        List<ReceiptDecisionRequest> decisions =
                toDecisionDtoList(receipts, approverId, approverName, 3, reason);

        // 일반 특정 사원 결재(영수증은 다건) 로직 재사용
        saveReceiptDecisions(approverId, decisions);
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
     *							결재선 첫번째에 '반려' 표시 + "[이름(아이디)] 강제적으로 반려처리 되었습니다" 사유
     *  ─ APPROVED / CLOSED  ➜	결재선 **전부 승인**(approvalStatus=true) 처리
     */
    @Override
    @Transactional
    public void forceChangeStatus(Integer receiptId,
                                  String  statusCode,
                                  Integer changerId,
                                  String changerName,
                                  ReceiptDeptTeamUpdate requestDto) {
        // 포트폴리오용: 기능 비활성화
        throw new RuntimeException("포트폴리오용: 영수증 상태 변경 기능이 비활성화되어 있습니다.");
    }
}
