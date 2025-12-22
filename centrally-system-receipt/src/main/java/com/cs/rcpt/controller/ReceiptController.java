package com.cs.rcpt.controller;

import com.cs.rcpt.controller.request.ReceiptDecisionRequest;
import com.cs.rcpt.controller.response.ReceiptPage;
import com.cs.rcpt.controller.response.ReceiptYearlySummaryResponse;
import com.cs.rcpt.entity.Receipt;
import com.cs.rcpt.enums.ReceiptProcessStatus;
import com.cs.rcpt.service.ReceiptService;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    /**
     * (페이징) 영수증 전체 조회
     * 예) GET /receipts?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<ReceiptPage<Receipt>> getReceipts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
    	Sort sort = Sort.by(
			Sort.Order.desc("submissionDate"),
    	    Sort.Order.desc("receiptId")
    	);
        ReceiptPage<Receipt> result = receiptService.getReceipts(page, size, sort);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    

    /**
     * (페이징) 사용자 영수증 전체 조회
     * 예) GET /receipts/{userId}?page=0&size=10
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ReceiptPage<Receipt>> getReceiptByUserId(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @PathVariable("userId") Integer userId
    ) {
    	Sort sort = Sort.by(
			Sort.Order.desc("submissionDate"),
    	    Sort.Order.desc("receiptId")
    	);
        ReceiptPage<Receipt> result = receiptService.getReceiptsByUserId(userId, page, size, sort);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    
    /**
     * (페이징) 사용자 영수증 통합 검색
     *
     * <pre>
     * ▶ GET /receipts/user/{userId}/search
     *
     * ▸ 사용 가능한 파라미터
     *   · statusCodes : 상태코드 배열
     *       └ 예) statusCodes=WAITING&statusCodes=REJECTED
     *   · yearMonth   : "YYYY-MM"   형식 (월별 조회)
     *   · startDate   : "YYYY-MM-DD" 시작일
     *   · endDate     : "YYYY-MM-DD" 종료일
     *   · page, size  : 페이징
     *
     * ▸ 검색 분기 우선순위
     *   1) yearMonth && 단일 statusCodes → 월 + 상태 1개
     *   2) statusCodes만                → 다중 상태 필터
     *   3) yearMonth만                  → 월별 조회
     *   4) startDate & endDate
     *        (+ 단일 statusCodes 허용) → 기간 조회
     * </pre>
     *
     * @param userId      	사용자 ID (path variable)
     * @param startDate   	기간 시작일 (nullable)
     * @param endDate     	기간 종료일 (nullable)
     * @param yearMonth   	"YYYY-MM" (nullable)
     * @param statusCodes 	상태코드 배열 (nullable)
     * @param page        	페이지 번호(0부터)
     * @param size        	페이지 크기
     * @return 영수증 페이지 객체
     */

    @GetMapping("/user/{userId}/search")
    public ResponseEntity<ReceiptPage<Receipt>> searchReceipts(
            /* PathVariable도 name 지정 */
            @PathVariable(name = "userId") Integer userId,

            /* ─────── 검색 파라미터 ─────── */
            @RequestParam(name = "startDate",   required = false) String startDate,
            @RequestParam(name = "endDate",     required = false) String endDate,
            @RequestParam(name = "yearMonth",   required = false) String yearMonth,
            @RequestParam(name = "statusCodes", required = false)
                List<ReceiptProcessStatus> statusCodes,

            /* ─────── 페이징 파라미터 ─────── */
            @RequestParam(name = "page", defaultValue = "0")  int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {

        Sort sort = Sort.by(
            Sort.Order.desc("submissionDate"),
            Sort.Order.desc("receiptId")
        );

        /* ────────────────────────────────────────────────────
         * ① yearMonth + statusCodes (단일만 허용)
         * ─────────────────────────────────────────────────── */
        if (yearMonth != null && !yearMonth.isBlank()
                && statusCodes != null && statusCodes.size() == 1) {

            // yearMonth → 날짜 범위 변환
            LocalDate ymStart = LocalDate.parse(yearMonth + "-01");
            LocalDate ymEnd   = ymStart.plusMonths(1).minusDays(1);

            ReceiptPage<Receipt> result =
                receiptService.getReceiptsBySearchUserDateRange(
                    userId,
                    ymStart.toString(),
                    ymEnd.toString(),
                    statusCodes.get(0),      // 단일 상태
                    page, size, sort);

            return ResponseEntity.ok(result);
        }

        /* ② statusCodes만 있을 때 (다중 허용) */
        if (statusCodes != null && !statusCodes.isEmpty()
                && yearMonth == null && startDate == null && endDate == null) {

            ReceiptPage<Receipt> result =
                receiptService.getReceiptsByUserIdAndStatuses(
                    userId, statusCodes, page, size, sort);
            return ResponseEntity.ok(result);
        }

        /* ③ yearMonth만 있을 때 */
        if (yearMonth != null && !yearMonth.isBlank()) {
            ReceiptPage<Receipt> result =
                receiptService.getReceiptByUserIdAndYearMonth(
                    userId, page, size, yearMonth, sort);
            return ResponseEntity.ok(result);
        }

        /* ④ 날짜 범위 (+단일 status) */
        if (startDate != null && endDate != null) {
            ReceiptProcessStatus single =
                (statusCodes != null && !statusCodes.isEmpty()) ? statusCodes.get(0) : null;

            ReceiptPage<Receipt> result =
                receiptService.getReceiptsBySearchUserDateRange(
                    userId, startDate, endDate, single, page, size, sort);
            return ResponseEntity.ok(result);
        }

        throw new IllegalArgumentException("검색 조건을 지정하세요.");
    }

    
    /**
     * 영수증 단일 조회
     * GET /receipts/{id}
     */
    @GetMapping("/{receiptId}")
    public ResponseEntity<Receipt> getReceipt(@PathVariable("receiptId") Integer id) {
        Receipt receipt = receiptService.getReceipt(id);
        if (receipt == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(receipt);
    }
    
    
    /**
     * (페이징) 전체 사용자 영수증 + ReceiptStatus 목록 조회 (유저 아이디 없이 조회)
     * 예) GET /receipts/with-status/all?yearMonth=2025-04&page=0&size=10
     */
    @GetMapping("/with-status/all")
    public ResponseEntity<ReceiptPage<Receipt>> getReceiptsWithStatus(
    		@RequestParam(name = "userId", required = false) Integer userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "startDate", required = false) String startDateStr,
            @RequestParam(name = "endDate", required = false) String endDateStr,
            @RequestParam(name = "yearMonth", required = false) String yearMonth
    ) {
    	Sort sort = Sort.by(
			Sort.Order.desc("submissionDate"),
    	    Sort.Order.desc("receiptId")
    	);
    	ReceiptPage<Receipt> receiptPage;
        
        if (yearMonth != null && !yearMonth.trim().isEmpty()) {
            // 월별 필터 조회 (전체 사용자)
            receiptPage = receiptService.getReceiptsByYearMonthForAll(page, size, yearMonth, sort);
        } else if (startDateStr != null && endDateStr != null) {
            // 날짜 범위 조회 (전체 사용자)
            receiptPage = receiptService.getReceiptsBySearchUserDateRange(userId, startDateStr, endDateStr, null, page, size, sort);
        } else {
            throw new IllegalArgumentException("기간을 정해주세요");
        }
        
        return new ResponseEntity<>(receiptPage, HttpStatus.OK);
    }

    
    
    /**
     * 특정 사용자 + 기간별 영수증 조회 (+ 상태 필터)
     * 예) GET /receipts/date-range?userId=1&startDate=2025-04-01&endDate=2025-04-09&statusCode=1&page=0&size=10
     */
    @GetMapping("/date-range")
    public ResponseEntity<ReceiptPage<Receipt>> getReceiptBySearchUserDateRange(
            @RequestParam(name = "userId", required = false) Integer userId,
            @RequestParam(name = "startDate", required = false) String startDateStr,
            @RequestParam(name = "endDate", required = false) String endDateStr,
            @RequestParam(name = "statusCode", required = false) ReceiptProcessStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        // 정렬: submissionDate 내림차순
    	Sort sort = Sort.by(
			Sort.Order.desc("submissionDate"),
    	    Sort.Order.desc("receiptId")
    	);

        // Service 호출
        ReceiptPage<Receipt> result = receiptService.getReceiptsBySearchUserDateRange(
                userId, startDateStr, endDateStr, status, page, size, sort
        );

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    
    
    /**
     * (연도별) 사용자 영수증 요약 조회
     * 예) GET /receipts/user/5/year-summary?year=2025
     */
    @GetMapping("/user/{userId}/year-summary")
    public ResponseEntity<ReceiptYearlySummaryResponse> getReceiptByUserYearlySummary(
        @PathVariable("userId") Integer userId,
        @RequestParam("year") int year
    ) {
        // (1) Service 호출
        ReceiptYearlySummaryResponse summary = receiptService.getReceiptByUserYearlySummary(userId, year);

        return new ResponseEntity<>(summary, HttpStatus.OK);
    }
    

    /**
     * 사용자 영수증 생성 (파일 + 폼 데이터)
     * POST /receipts/user/{userId}
     * 
     * @param userId      경로에서 받는 사용자 ID
     * @param date        "2025-04-01" 같은 형식
     * @param type        영수증 구분
     * @param amount      금액 (문자열)
     * @param reason      사유
     * @param people      JSON 배열(예: [{"name":"권매니저","department":"경영관리본부","team":"경영팀"}])
     * @param file        첨부파일 (단일)
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<Receipt> createUserReceipt(
            @PathVariable("userId")  	 Integer userId,
            @RequestParam("userName")    String  userName,
            @RequestParam("userEmail")    String  userEmail,

            /* ① 동일 이름으로 변경 */
            @RequestParam("date")        String  date,
            @RequestParam("categoryId")  Integer categoryId,
            @RequestParam("amount")      String  amount,
            @RequestParam("reason")      String  reason,

            /* ② 분리해서 받기 */
            @RequestParam(name="participants", required=false) String participants,
            @RequestParam(name="approvers",    required=false) String approvers,
            
            @RequestParam(name="departmentId",		required=false) Integer departmentId,
            @RequestParam(name="departmentName",	required=false) String  departmentName,
            @RequestParam(name="teamId",    	required=false) Integer teamId,
            @RequestParam(name="teamName",    	required=false) String  teamName,

            @RequestParam(name="receiptFile",  required=false) MultipartFile file)
    {
        Receipt saved = receiptService.createUserReceipt(
                userId, userName, userEmail, date, categoryId, amount, reason,
                participants, approvers, 
                departmentId, departmentName,
                teamId, teamName, file);          // ③ 서비스 시그니처 변경
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }
    

    /**
     * 영수증 수정
     * PUT /receipts/{id}
     */
    @PutMapping("/{receiptId}")
    public ResponseEntity<Receipt> updateReceipt(
    		@PathVariable("receiptId") Integer id,
            @RequestBody Receipt updated
    ) {
        Receipt result = receiptService.updateReceipt(id, updated);
        return ResponseEntity.ok(result);
    }
    
    
    /**
     * 영수증 수정
     * PATCH /user/{userId}/{receiptId}
     */
    @PatchMapping("/user/{userId}/{receiptId}")
    public ResponseEntity<Receipt> patchReceipt(
            @PathVariable("userId")    Integer userId,
            @PathVariable("receiptId") Integer receiptId,

            /* ───────── Optional fields ───────── */
            @RequestParam(name = "date",         required = false) String  date,
            @RequestParam(name = "categoryId",   required = false) Integer categoryId,
            @RequestParam(name = "amount",       required = false) String  amount,
            @RequestParam(name = "reason",       required = false) String  reason,
            @RequestParam(name = "participants", required = false) String  participants,
            @RequestParam(name = "approvers",    required = false) String  approvers,
            @RequestParam(name = "receiptFile",  required = false) MultipartFile receiptFile,
            @RequestParam(name = "deleteFile",   required = false) Boolean deleteFile
    ) {
        Receipt saved = receiptService.patchReceipt(
                userId, receiptId, date, categoryId, amount, reason,
                participants, approvers, receiptFile, deleteFile);
        return ResponseEntity.ok(saved);
    }

    /**
     * 영수증 삭제
     * DELETE /receipts/{id}
     */
    @DeleteMapping("/{receiptId}")
    public ResponseEntity<Void> deleteReceipt(@PathVariable("receiptId") Integer id) {
        receiptService.deleteReceipt(id);
        return ResponseEntity.noContent().build();
    }
    
    
    
}
