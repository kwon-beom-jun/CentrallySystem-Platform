package com.cs.rcpt.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cs.core.security.CustomUserPrincipal;
import com.cs.rcpt.controller.request.BulkRejectAndRemoveRequest;
import com.cs.rcpt.controller.request.BulkUsersActionRequest;
import com.cs.rcpt.controller.request.ReceiptDecisionRequest;
import com.cs.rcpt.controller.request.ReceiptDeptTeamUpdate;
import com.cs.rcpt.service.ReceiptRequestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/request")
@RequiredArgsConstructor
public class ReceiptRequestController {

    private final ReceiptRequestService receiptRequestService;

    /** -------------------------------------------------
     * [신청] 영수증 ‘REQUEST’ 상태 전환
     * ─ POST /request/{receiptId}
     * @param receiptId  영수증 PK
     * @param requestDto 부서·팀 정보 DTO
     * @param user       로그인 사용자 정보
     * -------------------------------------------------- */
    @PostMapping("/{receiptId}")
    public ResponseEntity<Void> requestReceipt(
            @PathVariable("receiptId") int receiptId,
            @RequestBody ReceiptDeptTeamUpdate requestDto,
            @AuthenticationPrincipal CustomUserPrincipal user  // 사용 중인 UserDetails 구현체
    ) {
        receiptRequestService.requestReceipt(receiptId, user.getUserId(), requestDto);
        return ResponseEntity.ok().build();
    }
    
    /** -------------------------------------------------
     * [단건결재] 특정 사원 영수증 승인/반려 저장
     * ─ POST /request/user/{userId}/decisions
     * @param userId     신청자 PK
     * @param decisions  ReceiptDecisionRequest 리스트
     * -------------------------------------------------- */
    @PostMapping("/user/{userId}/decisions")
    public ResponseEntity<Void> saveReceiptDecisions(
            @PathVariable("userId") Integer userId,
            @RequestBody List<ReceiptDecisionRequest> decisions
    ) {
    	receiptRequestService.saveReceiptDecisions(userId, decisions);
        return ResponseEntity.ok().build();
    }

    /** -------------------------------------------------
     * [일괄승인] 내 차례 영수증 다건 승인 (날짜 필터)
     * ─ POST /request/bulk-users-approval
     * @param req   BulkUsersActionRequest (userIds, startDate, endDate)
     * @param user  로그인 사용자 정보
     * -------------------------------------------------- */
    @PostMapping("/bulk-users-approval")
    public ResponseEntity<Void> bulkUsersApproval(
            @RequestBody @Valid BulkUsersActionRequest req,
            @AuthenticationPrincipal CustomUserPrincipal user) {

        receiptRequestService.bulkApprove(
            user.getUserId(),
            user.getUsername(), 
            req.getUserIds(),
            req.getStartDate(),
            req.getEndDate(),
            req.getRoles());

        return ResponseEntity.ok().build();
    }

    /** -------------------------------------------------
     * [일괄반려] 내 차례 영수증 다건 반려 (날짜 필터)
     * ─ POST /request/bulk-users-rejected
     * @param req    BulkUsersActionRequest (userIds, startDate, endDate, reason)
     * @param user   로그인 사용자 정보
     * -------------------------------------------------- */
    @PostMapping("/bulk-users-rejected")
    public ResponseEntity<Void> bulkUsersRejected(
            @RequestBody @Valid BulkUsersActionRequest req,
            @AuthenticationPrincipal CustomUserPrincipal user) {

        receiptRequestService.bulkReject(
            user.getUserId(),
            user.getUsername(), 
            req.getUserIds(),
            req.getStartDate(),
            req.getEndDate(),
            req.getReason(),
            req.getRoles());

        return ResponseEntity.ok().build();
    }

    /** -------------------------------------------------
     * [일괄마감] 영수증 CLOSED 상태 일괄 변경
     * ─ POST /request/bulk-receipts-close
     * @param receiptIds 마감 대상 영수증 PK 리스트
     * -------------------------------------------------- */
    @PostMapping("/bulk-receipts-close")
    public ResponseEntity<Void> closeReceipts(@RequestBody List<Integer> receiptIds) {
    	receiptRequestService.closeReceipts(receiptIds);
        return ResponseEntity.ok().build();
    }

    /** -------------------------------------------------
     * [일괄반려] 관리자용 영수증 일괄 반려 (사유 포함)
     * ─ POST /request/bulk-receipts-reject?reason=...
     * @param receiptIds  반려 대상 영수증 PK 리스트
     * @param reason      반려 사유
     * @param user        로그인 사용자 정보
     * -------------------------------------------------- */
    @PostMapping("/bulk-receipts-reject")
    public ResponseEntity<Void> rejectReceipts(
    		@RequestBody List<Integer> receiptIds,
            @RequestParam(name = "reason") String reason,
            @AuthenticationPrincipal CustomUserPrincipal user // 변경자
    ) {
    	receiptRequestService.rejectReceipts(receiptIds, reason, user.getUserId(), user.getUsername());
        return ResponseEntity.ok().build();
    }


    /** -------------------------------------------------
     * [사용자비활성화] 결재자 제거 및 후속 처리
     * ─ POST /request/bulk-reject-and-remove?reason=...
     * @param request  BulkRejectAndRemoveRequest (receiptIds, userToRemoveId)
     * @param reason   반려 사유
     * @param user     로그인 사용자 정보
     * -------------------------------------------------- */
    @PostMapping("/bulk-reject-and-remove")
    public ResponseEntity<Void> rejectAndRemoveApprover(
            @RequestBody BulkRejectAndRemoveRequest request, // 새로 만든 DTO 사용
            @RequestParam(name = "reason") String reason,
            @AuthenticationPrincipal CustomUserPrincipal user) {
        
        receiptRequestService.rejectAndRemoveApprover(
            request.getReceiptIds(),
            request.getUserToRemoveId(),
            reason,
            user.getUserId(),
            user.getUsername()
        );
        return ResponseEntity.ok().build();
    }
    

    /** -------------------------------------------------
     * [강제변경] 관리자용 영수증 상태/결재선 리셋
     * ─ PUT /request/{receiptId}/status/{statusCode}
     * @param receiptId   영수증 PK
     * @param statusCode  목표 상태 코드 (REQUEST, WAITING, REJECTED, APPROVED, CLOSED)
     * @param requestDto  부서·팀 정보 DTO (선택)
     * @param user        로그인 사용자 정보
     * -------------------------------------------------- */
    @PutMapping("/{receiptId}/status/{statusCode}")
    public ResponseEntity<Void> forceChangeStatus(
            @PathVariable("receiptId") Integer receiptId,
            @PathVariable("statusCode") String  statusCode,
            @RequestBody(required = false) ReceiptDeptTeamUpdate requestDto,
            @AuthenticationPrincipal CustomUserPrincipal user // 변경자
    ) {
    	receiptRequestService.forceChangeStatus(receiptId, statusCode, user.getUserId(), user.getUsername(), requestDto);
        return ResponseEntity.ok().build();
    }
}
