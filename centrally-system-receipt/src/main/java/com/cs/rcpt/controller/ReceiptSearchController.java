package com.cs.rcpt.controller;

import com.cs.rcpt.controller.response.ReceiptApprClosedSummaryDto;
import com.cs.rcpt.controller.response.ReceiptHistoryOverviewDto;
import com.cs.rcpt.controller.response.ReceiptPage;
import com.cs.rcpt.controller.response.ReceiptReportPageDto;
import com.cs.rcpt.controller.response.ReceiptUserSummaryDto;
import com.cs.rcpt.entity.Receipt;
import com.cs.rcpt.enums.ReceiptProcessStatus;
import com.cs.rcpt.service.ReceiptSearchService;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *	영수증 특정 내역 조회
 */
@RestController
@RequestMapping("/receipts-search")
@RequiredArgsConstructor
public class ReceiptSearchController {

    private final ReceiptSearchService receiptSearchService;

    /**
     * 📄 [집계] 결재자(approverId) 기준 신청자별 건수·금액 요약 (페이징)
     *
     * GET /receipts-search?approverId&startDate&endDate&[userId]&[statusCodes[]]
     */
    @GetMapping()
    public ResponseEntity<ReceiptPage<ReceiptUserSummaryDto>> getSummary(
            @RequestParam("approverId") Integer approverId,
            @RequestParam(value = "userId", required = false) Integer userId,
            @RequestParam("startDate") String  startDate,
            @RequestParam("endDate") String  endDate,       
            @RequestParam(value = "statusCodes", required = false) List<ReceiptProcessStatus> statusCodes, 
            @RequestParam(value = "page", defaultValue = "0")  int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        LocalDate s = LocalDate.parse(startDate);
        LocalDate e = LocalDate.parse(endDate);

        ReceiptPage<ReceiptUserSummaryDto> pg =
    		receiptSearchService.getSummaryOnlyForMe(approverId, userId, s, e, statusCodes, page, size);

        return ResponseEntity.ok(pg);
    }

    /**
     * 📜 [개요] 결재자 관점 – 기간 내 요약 + 내 차례 건수 + 최근 활동(간단)
     * GET /receipts-search/approver/{approverId}/history-overview?startDate&endDate&limit=8
     */
    @GetMapping("/approver/{approverId}/history-overview")
    public ResponseEntity<ReceiptHistoryOverviewDto> getHistoryOverview(
            @PathVariable("approverId") Integer approverId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "limit", defaultValue = "8") int limit
    ) {
        return ResponseEntity.ok(
            receiptSearchService.buildHistoryOverview(approverId, startDate, endDate, limit)
        );
    }
    
    /**
     * 📊 [통계] 기간·필터별 전사 합계 (1 row)
     *
     * GET /receipts-search/totals
     */
    @GetMapping("/totals")
    public ResponseEntity<Map<String,Object>> getTotals(
            @RequestParam String  startDate,
            @RequestParam String  endDate,
            @RequestParam(required = false) Integer approverId,
            @RequestParam(required = false) Integer userId,
            @RequestParam(name = "statusCodes", required = false) List<ReceiptProcessStatus> statusCodes) {

        Map<String,Object> dto = receiptSearchService.getGlobalTotals(
                LocalDate.parse(startDate),
                LocalDate.parse(endDate),
                approverId,
                userId,
                statusCodes);

        return ResponseEntity.ok(dto);
    }


    /**
     * 📄 [목록] ‘내 결재선’ 영수증 목록 (페이징)
     *
     * GET /receipts-search/approver/{approverId}/date-range
     *  ├─ 기간(startDate, endDate) 필수
     *  └─ userId(신청자), statusCode(단일) 선택
     */
    @GetMapping("/approver/{approverId}/date-range")
    public ResponseEntity<ReceiptPage<Receipt>> getMyPendingByDate(
            @PathVariable("approverId") Integer approverId,
            @RequestParam(name="userId", required=false) Integer userId,
            @RequestParam(name="startDate", required=false) String  startDate,
            @RequestParam(name="endDate", required=false) String  endDate,
            
	        @RequestParam(name="statusCodes", required=false)
	            List<ReceiptProcessStatus> statusCodes,
	        @RequestParam(name="statusCode", required=false)
	            ReceiptProcessStatus statusCode,
            
            @RequestParam(name="page", defaultValue="0") int page,
            @RequestParam(name="size", defaultValue="10") int size) {

        Sort sort = Sort.by(
            Sort.Order.desc("submissionDate"),
            Sort.Order.desc("receiptId"));

        /* 단일 값이 오면 리스트로 변환 */
        if (statusCodes == null && statusCode != null)
            statusCodes = List.of(statusCode);
        
        ReceiptPage<Receipt> pg = receiptSearchService.getMyPendingByDate(
        		approverId, userId, startDate, endDate, statusCodes, page, size, sort
    		);

        return ResponseEntity.ok(pg);
    }
    
    
    /**
     * 📄 [목록] ‘내 결재선, 내 차례’(REQUEST) + 결재/합의(1·2·3) (페이징)
     *
     * GET /receipts-search/approver/{approverId}/date-range/pending-role
     *   ├─ 기간(startDate, endDate) 필수
     *   ├─ userId(신청자) 선택
     *   └─ roles(결재/합의) 선택
     */
    @GetMapping("/approver/{approverId}/date-range/pending")
    public ResponseEntity<ReceiptPage<Receipt>> getMyPendingByRoles(
            @PathVariable("approverId") Integer approverId,
            @RequestParam(name="userId", required=false) Integer userId,
            @RequestParam(name="startDate") String startDate,
            @RequestParam(name="endDate")   String endDate,
            @RequestParam(name="roles", required=false) List<Integer> roles, // ①
            @RequestParam(name="page", defaultValue="0")  int page,
            @RequestParam(name="size", defaultValue="10") int size) {

        Sort sort = Sort.by(
            Sort.Order.desc("submissionDate"),
            Sort.Order.desc("receiptId"));

        ReceiptPage<Receipt> pg =
            receiptSearchService.getMyPendingByDateRoles(
                approverId, userId, startDate, endDate, roles, page, size, sort);

        return ResponseEntity.ok(pg);
    }

    /**
     * 📄 [목록] 대리결재자 관점 ‘내 차례’ + 결재/합의(1·2·3) (페이징)
     * GET /receipts-search/delegate/{delegateId}/date-range/pending
     */
    @GetMapping("/delegate/{delegateId}/date-range/pending")
    public ResponseEntity<ReceiptPage<Receipt>> getMyPendingByRolesAsDelegate(
            @PathVariable("delegateId") Integer delegateId,
            @RequestParam(name="userId", required=false) Integer userId,
            @RequestParam(name="startDate") String startDate,
            @RequestParam(name="endDate")   String endDate,
            @RequestParam(name="roles", required=false) List<Integer> roles,
            @RequestParam(name="page", defaultValue="0") int page,
            @RequestParam(name="size", defaultValue="10") int size) {

        Sort sort = Sort.by(
            Sort.Order.desc("submissionDate"),
            Sort.Order.desc("receiptId")
        );

        ReceiptPage<Receipt> pg = receiptSearchService.getMyPendingByDateRolesAsDelegate(
            delegateId, userId, startDate, endDate, roles, page, size, sort);

        return ResponseEntity.ok(pg);
    }

    /**
     * 📄 [목록] 대리결재자 관점 전체 상태 조회 (페이징)
     * GET /receipts-search/delegate/{delegateId}/date-range
     */
    @GetMapping("/delegate/{delegateId}/date-range")
    public ResponseEntity<ReceiptPage<Receipt>> getByDelegateAndDateRange(
            @PathVariable("delegateId") Integer delegateId,
            @RequestParam(name="userId", required=false) Integer userId,
            @RequestParam(name="startDate") String startDate,
            @RequestParam(name="endDate")   String endDate,
            @RequestParam(name="statusCodes", required=false) List<ReceiptProcessStatus> statusCodes,
            @RequestParam(name="page", defaultValue="0") int page,
            @RequestParam(name="size", defaultValue="10") int size) {

        Sort sort = Sort.by(
            Sort.Order.desc("submissionDate"),
            Sort.Order.desc("receiptId")
        );

        ReceiptPage<Receipt> pg = receiptSearchService.getByDelegateAndDateRange(
            delegateId, userId, startDate, endDate, statusCodes, page, size, sort);

        return ResponseEntity.ok(pg);
    }

    /**
     * 📄 [요약] 대리결재자 관점 신청자별 집계 (전체 상태)
     * GET /receipts-search/delegate/{delegateId}/summary
     */
    @GetMapping("/delegate/{delegateId}/summary")
    public ResponseEntity<ReceiptPage<ReceiptUserSummaryDto>> getSummaryForDelegate(
            @PathVariable("delegateId") Integer delegateId,
            @RequestParam(name="userId", required=false) Integer userId,
            @RequestParam("startDate") String  startDate,
            @RequestParam("endDate")   String  endDate,
            @RequestParam(name="statusCodes", required=false) List<ReceiptProcessStatus> statusCodes,
            @RequestParam(name="page", defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "10") int size) {

        ReceiptPage<ReceiptUserSummaryDto> pg =
            receiptSearchService.getSummaryForDelegate(
                delegateId, userId,
                LocalDate.parse(startDate), LocalDate.parse(endDate),
                statusCodes, page, size);

        return ResponseEntity.ok(pg);
    }
    /**
     * 📄 [요약] 대리결재자 관점 ‘내 차례’ – 신청자별 집계
     * GET /receipts-search/delegate/{delegateId}/pending-summary
     */
    @GetMapping("/delegate/{delegateId}/pending-summary")
    public ResponseEntity<ReceiptPage<ReceiptUserSummaryDto>> getPendingSummaryByUserAsDelegate(
            @PathVariable("delegateId") Integer delegateId,
            @RequestParam(name="userId", required=false) Integer userId,
            @RequestParam("startDate") String  startDate,
            @RequestParam("endDate")   String  endDate,
            @RequestParam(name="roles", required=false) List<Integer> roles,
            @RequestParam(name="page", defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "10") int size) {

        ReceiptPage<ReceiptUserSummaryDto> pg =
            receiptSearchService.getPendingSummaryByUserAsDelegate(
                delegateId, userId,
                LocalDate.parse(startDate), LocalDate.parse(endDate),
                roles, page, size);

        return ResponseEntity.ok(pg);
    }

    
    
    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<ReceiptPage<Receipt>> getByUserAndDate(
            @PathVariable(name="userId") Integer userId,
            @RequestParam(name="startDate", required=false) String  startDate,
            @RequestParam(name="endDate", required=false) String  endDate,
            @RequestParam(name = "statusCodes", required = false) List<ReceiptProcessStatus> statusCodes,
            @RequestParam(name = "page", defaultValue = "0")  int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        ReceiptPage<Receipt> pg =
             receiptSearchService.getByUserAndDate(
                     userId,
                     startDate, endDate,
                     statusCodes, page, size);

        return ResponseEntity.ok(pg);
    }

    /**
     * 📊 [통계] ‘내 결재선’ 건수·금액·평균
     *
     * GET /receipts-search/approver/{approverId}/date-range/summary
     */
    @GetMapping("/approver/{approverId}/date-range/summary")
    public ResponseEntity<Map<String, Long>> getMyPendingSummary(
            @PathVariable("approverId") Integer approverId,
            @RequestParam(name="userId", required=false) Integer searchUserId,
            @RequestParam("startDate") String  startDate,
            @RequestParam("endDate")   String  endDate,
            @RequestParam(name="statusCode", required=false) ReceiptProcessStatus status) {

        Map<String, Long> stats = receiptSearchService.getTotalsForMyPending(
            approverId, searchUserId, startDate, endDate, status);

        return ResponseEntity.ok(stats);
    }
    
    /**
     * 📄 [요약] ‘내 차례’ 영수증 – 신청자별 집계
     * GET /receipts-search/approver/{approverId}/pending-summary
     */
    @GetMapping("/approver/{approverId}/pending-summary")
    public ResponseEntity<ReceiptPage<ReceiptUserSummaryDto>> getPendingSummaryByUser(
            @PathVariable("approverId") Integer approverId,
            @RequestParam(name="userId", required=false) Integer userId,
            @RequestParam(name="departmentId", required = false) Integer departmentId, 
            @RequestParam(name="teamId", required = false) Integer teamId, 
            @RequestParam("startDate") String  startDate,
            @RequestParam("endDate")   String  endDate,
            @RequestParam(name="roles", required=false) List<Integer> roles,
            @RequestParam(name="page", defaultValue = "0") int page,
            @RequestParam(name="size", defaultValue = "10") int size)
    {
        ReceiptPage<ReceiptUserSummaryDto> pg =
            receiptSearchService.getPendingSummaryByUser(
                approverId, userId, departmentId, teamId,
                LocalDate.parse(startDate), LocalDate.parse(endDate),
                roles, page, size);

        return ResponseEntity.ok(pg);
    }

    /**
     * 📄 [집계] (전사) 승인·마감 상태 요약 + 과거 승인 월 목록
     * GET /receipts-search/appr-closed?startDate=...&endDate=...&page=0&size=10
     */
    @GetMapping("/appr-closed")
    public ResponseEntity<ReceiptReportPageDto> getApprClosedSummaryAll( // ✨ 반환 타입 변경
            @RequestParam(name = "userId", required = false) Integer userId,
            @RequestParam(name = "startDate") String startDate,
            @RequestParam(name = "endDate") String endDate,
            @RequestParam(name = "statusCodes", required = false) List<ReceiptProcessStatus> statusCodes,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        if (statusCodes == null || statusCodes.isEmpty()) {
            statusCodes = List.of(
                ReceiptProcessStatus.APPROVED,
                ReceiptProcessStatus.CLOSED
            );
        }
        
        // 1. 기존 페이징 데이터 조회
        ReceiptPage<ReceiptApprClosedSummaryDto> pg =
            receiptSearchService.getApprClosedSummary(
                null, userId, LocalDate.parse(startDate), LocalDate.parse(endDate),
                statusCodes, page, size);
        
        // 2. 과거 승인 월 목록 조회 (startDate의 YYYY-MM 값을 기준)
        String currentMonth = startDate.substring(0, 7);
        List<String> pastMonths = receiptSearchService.findPastApprovedMonths(currentMonth);

        // 3. 두 데이터를 합쳐서 새로운 DTO로 반환
        ReceiptReportPageDto responseDto = ReceiptReportPageDto.builder()
                .pageData(pg)
                .pastApprovedMonths(pastMonths)
                .build();
        
        return ResponseEntity.ok(responseDto);
    }
    
    
    /**
     * 📊 [통계] ‘내 차례’ + 역할별(결재/합의) 금액·건수·평균
     *
     * GET /receipts-search/approver/{approverId}/date-range/summary/pending
     *   ├─ 필수: startDate, endDate
     *   ├─ 선택: userId
     *   └─ 선택: roles[]=1(결재)·2·3(합의)
     */
    @GetMapping("/approver/{approverId}/date-range/summary/pending")
    public ResponseEntity<Map<String, Long>> getMyPendingSummaryByRoles(
    		@PathVariable("approverId") Integer approverId,
    		@RequestParam(name = "userId", required = false) Integer userId,
            @RequestParam(name="departmentId", required = false) Integer departmentId, 
            @RequestParam(name="teamId", required = false) Integer teamId, 
            @RequestParam(name = "startDate")  String startDate,
            @RequestParam(name = "endDate")    String endDate,
            @RequestParam(name="roles", required=false) List<Integer> roles) {

        Map<String, Long> stats =
            receiptSearchService.getTotalsForMyPendingRoles(
                approverId, userId, departmentId, teamId, startDate, endDate, roles);

        return ResponseEntity.ok(stats);
    }

    
    /**
     * 📄 [목록] 사용자 비활성화 및 권한 변경으로 인한 영수증 결재선 목록 조회 (페이징)
     * 	결재선에서 삭제되는 요건
     * 		1. 대기	: 신청시 문제 발생
     * 		2. 신청	: 승인여부 상관없음, 만약 반려 후 재신청시 해당 유저 남아있어 문제 발생
     * 		3. 반려	: 재신청시 문제 발생
     *
     * GET /receipts-search/approver/{approverId}/pending-list
     * @param approverId 결재자 ID
     * @param page       페이지 번호
     * @param size       페이지 크기
     * @return 페이징된 영수증 목록
     */
    @GetMapping("/approver/{approverId}/pending-list")
    public ResponseEntity<ReceiptPage<Receipt>> getMyPendingApprovals(
            @PathVariable("approverId") Integer approverId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        ReceiptPage<Receipt> pagedResult = receiptSearchService.getMyPendingApprovals(approverId, page, size);
        return ResponseEntity.ok(pagedResult);
    }

    /**
     * 🛎️ [알림] ‘내 차례’ 결재·합의 건수
     * GET /receipts-search/approver/{approverId}/pending-counts
     */
    @GetMapping("/approver/{approverId}/pending-counts")
    public ResponseEntity<Map<String, Object>> getPendingCounts(
            @PathVariable("approverId") Integer approverId) {

        Map<String, Object> result = receiptSearchService.getPendingCountsByRole(approverId);
        return ResponseEntity.ok(result);
    }

}
