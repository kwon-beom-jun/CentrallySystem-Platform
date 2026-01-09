//package com.cs.rcpt.controller;
//
//import com.cs.rcpt.entity.Receipt;
//import com.cs.rcpt.entity.ReceiptCategory;
//import com.cs.rcpt.entity.ReceiptStatus;
//import com.cs.rcpt.service.ReceiptService;
//import com.cs.rcpt.service.reponse.ReceiptPage;
//import com.cs.rcpt.service.reponse.ReceiptsWithStatusesResponse;
//import com.cs.rcpt.service.request.ReceiptDecisionRequest;
//import com.cs.rcpt.service.reponse.ReceiptYearlySummaryResponse;
//
//import lombok.RequiredArgsConstructor;
//
//import java.util.List;
//
//import org.springframework.data.domain.Sort;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//@RestController
//@RequestMapping("/receipts")
//@RequiredArgsConstructor
//public class ReceiptController {
//
//    private final ReceiptService receiptService;
//
//    /**
//     * (페이징) 영수증 전체 조회
//     * 예) GET /receipts?page=0&size=10
//     */
//    @GetMapping
//    public ResponseEntity<ReceiptPage> getReceipts(
//            @RequestParam(name = "page", defaultValue = "0") int page,
//            @RequestParam(name = "size", defaultValue = "10") int size
//    ) {
//    	Sort sort = Sort.by(
//			Sort.Order.desc("submissionDate"),
//    	    Sort.Order.desc("receiptId")
//    	);
//        ReceiptPage result = receiptService.getReceipts(page, size, sort);
//        return new ResponseEntity<>(result, HttpStatus.OK);
//    }
//    
//
//    /**
//     * (페이징) 사용자 영수증 전체 조회
//     * 예) GET /receipts/{userId}?page=0&size=10
//     */
//    @GetMapping("/user/{userId}")
//    public ResponseEntity<ReceiptPage> getReceiptByUserId(
//            @RequestParam(name = "page", defaultValue = "0") int page,
//            @RequestParam(name = "size", defaultValue = "10") int size,
//            @PathVariable("userId") Integer userId
//    ) {
//    	Sort sort = Sort.by(
//			Sort.Order.desc("submissionDate"),
//    	    Sort.Order.desc("receiptId")
//    	);
//        ReceiptPage result = receiptService.getReceiptsByUserId(userId, page, size, sort);
//        return new ResponseEntity<>(result, HttpStatus.OK);
//    }
//    
//    
//    /**
//     * (페이징) 사용자 영수증 – 특정 상태만 조회
//     * 예) GET /receipts/user/5/status?statusCode=1&page=0&size=10
//     */
//    @GetMapping("/user/{userId}/status")
//    public ResponseEntity<ReceiptPage> getReceiptsByUserIdAndStatus(
//            @PathVariable("userId") Integer userId,
//            @RequestParam("statusCode") Integer statusCode,   // 필수: 1,2,3…
//            @RequestParam(name = "page", defaultValue = "0") int page,
//            @RequestParam(name = "size", defaultValue = "10") int size
//    ) {
//        Sort sort = Sort.by(
//            Sort.Order.desc("submissionDate"),
//            Sort.Order.desc("receiptId")
//        );
//
//        ReceiptPage result =
//            receiptService.getReceiptsByUserIdAndStatus(userId, statusCode, page, size, sort);
//
//        return new ResponseEntity<>(result, HttpStatus.OK);
//    }
//    
//    /**
//     * 영수증 단일 조회
//     * GET /receipts/{id}
//     */
//    @GetMapping("/{receiptId}")
//    public ResponseEntity<Receipt> getReceipt(@PathVariable("receiptId") Integer id) {
//        Receipt receipt = receiptService.getReceipt(id);
//        if (receipt == null) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(receipt);
//    }
//    
//    
////    /**
////     * (페이징) 특정 사용자 영수증 + ReceiptStatus 목록 조회
////     * 예) GET /receipts/user/5/with-status?page=0&size=10
////     */
////    @GetMapping("/user/{userId}/with-status")
////    public ResponseEntity<ReceiptsWithStatusesResponse> getReceiptsByUserIdWithStatus(
////            @PathVariable("userId") Integer userId,
////            @RequestParam(name = "page", defaultValue = "0") int page,
////            @RequestParam(name = "size", defaultValue = "10") int size
////    ) {
////        // 정렬(내림차순) 설정 - submissionDate 기준
////        Sort sort = Sort.by(Sort.Direction.DESC, "submissionDate");
////
////        // (1) 해당 유저 영수증 페이징 조회
////        ReceiptPage receiptPage = receiptService.getReceiptsByUserId(userId, page, size, sort);
////
////        // (2) 모든 상태 코드 조회
////        List<ReceiptStatus> statusList = receiptService.getStatuses();
////
////        // (3) 두 데이터를 합쳐서 DTO 생성
////        ReceiptsWithStatusesResponse response = ReceiptsWithStatusesResponse.builder()
////                .receiptPage(receiptPage)
////                .statusList(statusList)
////                .build();
////
////        return new ResponseEntity<>(response, HttpStatus.OK);
////    }
//    
//    
//    /**
//     * (페이징) 사용자 영수증 + ReceiptStatus 목록 조회 (월별 필터 포함)
//     * 예) GET /receipts/user/5/with-status?month=08&page=0&size=10
//     */
//    @GetMapping("/user/{userId}/with-status")
//    public ResponseEntity<ReceiptPage> getReceiptByUserIdWithStatus(
//        @PathVariable("userId") Integer userId,
//        @RequestParam(name = "page", defaultValue = "0") int page,
//        @RequestParam(name = "size", defaultValue = "10") int size,
//        @RequestParam(name = "startDate", required = false) String startDateStr,
//        @RequestParam(name = "endDate", required = false) String endDateStr,
//        @RequestParam(name = "yearMonth", required = false) String yearMonth // ex) "2025-04"
//    ) {
//    	Sort sort = Sort.by(
//			Sort.Order.desc("submissionDate"),
//    	    Sort.Order.desc("receiptId")
//    	);
//        ReceiptPage receiptPage;
//
//        if (yearMonth != null && !yearMonth.trim().isEmpty()) {
//            // yearMonth 기준 조회
//            receiptPage = receiptService.getReceiptByUserIdAndYearMonth(userId, page, size, yearMonth, sort);
//
//        } else if (startDateStr != null && endDateStr != null) {
//            // 날짜 범위 기준 조회 (statusCode 없이)
//            receiptPage = receiptService.getReceiptsBySearchUserDateRange(
//                userId, startDateStr, endDateStr, null, page, size, sort);
//
//        } else {
//        	throw new IllegalArgumentException("기간을 정해주세요");
//        }
//
//        return new ResponseEntity<>(receiptPage, HttpStatus.OK);
//    }
//    
//    
//    /**
//     * (페이징) 전체 사용자 영수증 + ReceiptStatus 목록 조회 (유저 아이디 없이 조회)
//     * 예) GET /receipts/with-status/all?yearMonth=2025-04&page=0&size=10
//     */
//    @GetMapping("/with-status/all")
//    public ResponseEntity<ReceiptPage> getReceiptsWithStatus(
//    		@RequestParam(name = "userId", required = false) Integer userId,
//            @RequestParam(name = "page", defaultValue = "0") int page,
//            @RequestParam(name = "size", defaultValue = "10") int size,
//            @RequestParam(name = "startDate", required = false) String startDateStr,
//            @RequestParam(name = "endDate", required = false) String endDateStr,
//            @RequestParam(name = "yearMonth", required = false) String yearMonth
//    ) {
//    	Sort sort = Sort.by(
//			Sort.Order.desc("submissionDate"),
//    	    Sort.Order.desc("receiptId")
//    	);
//        ReceiptPage receiptPage;
//        
//        if (yearMonth != null && !yearMonth.trim().isEmpty()) {
//            // 월별 필터 조회 (전체 사용자)
//            receiptPage = receiptService.getReceiptsByYearMonthForAll(page, size, yearMonth, sort);
//        } else if (startDateStr != null && endDateStr != null) {
//            // 날짜 범위 조회 (전체 사용자)
//            receiptPage = receiptService.getReceiptsBySearchUserDateRange(userId, startDateStr, endDateStr, null, page, size, sort);
//        } else {
//            throw new IllegalArgumentException("기간을 정해주세요");
//        }
//        
//        return new ResponseEntity<>(receiptPage, HttpStatus.OK);
//    }
//
//    
//    
//    /**
//     * 특정 사용자 + 기간별 영수증 조회 (+ 상태 필터)
//     * 예) GET /receipts/date-range?userId=1&startDate=2025-04-01&endDate=2025-04-09&statusCode=1&page=0&size=10
//     */
//    @GetMapping("/date-range")
//    public ResponseEntity<ReceiptPage> getReceiptBySearchUserDateRange(
//            @RequestParam(name = "userId", required = false) Integer userId,
//            @RequestParam(name = "startDate", required = false) String startDateStr,
//            @RequestParam(name = "endDate", required = false) String endDateStr,
//            @RequestParam(name = "statusCode", required = false) Integer statusCode,
//            @RequestParam(name = "page", defaultValue = "0") int page,
//            @RequestParam(name = "size", defaultValue = "10") int size
//    ) {
//        // 정렬: submissionDate 내림차순
//    	Sort sort = Sort.by(
//			Sort.Order.desc("submissionDate"),
//    	    Sort.Order.desc("receiptId")
//    	);
//
//        // Service 호출
//        ReceiptPage result = receiptService.getReceiptsBySearchUserDateRange(
//                userId, startDateStr, endDateStr, statusCode, page, size, sort
//        );
//
//        return new ResponseEntity<>(result, HttpStatus.OK);
//    }
//    
//    
//    /**
//     * (연도별) 사용자 영수증 요약 조회
//     * 예) GET /receipts/user/5/year-summary?year=2025
//     */
//    @GetMapping("/user/{userId}/year-summary")
//    public ResponseEntity<ReceiptYearlySummaryResponse> getReceiptByUserYearlySummary(
//        @PathVariable("userId") Integer userId,
//        @RequestParam("year") int year
//    ) {
//        // (1) Service 호출
//        ReceiptYearlySummaryResponse summary = receiptService.getReceiptByUserYearlySummary(userId, year);
//
//        return new ResponseEntity<>(summary, HttpStatus.OK);
//    }
//    
//
//    /**
//     * 사용자 영수증 생성 (파일 + 폼 데이터)
//     * POST /receipts/user/{userId}
//     * 
//     * @param userId      경로에서 받는 사용자 ID
//     * @param date        "2025-04-01" 같은 형식
//     * @param type        영수증 구분
//     * @param amount      금액 (문자열)
//     * @param reason      사유
//     * @param people      JSON 배열(예: [{"name":"권매니저","department":"경영관리본부","team":"경영팀"}])
//     * @param file        첨부파일 (단일)
//     */
//    @PostMapping("/user/{userId}")
//    public ResponseEntity<Receipt> createUserReceipt(
//            @PathVariable("userId") Integer userId,
//            @RequestParam("date") String date,
//            @RequestParam("typeId") Integer categoryId,
//            @RequestParam("amount") String amount,
//            @RequestParam("reason") String reason,
//            @RequestParam(name = "people", required = false) String people,
//            @RequestParam(name = "receiptFile", required = false) MultipartFile file
//    ) {
//        // Service에서 저장 로직 처리
//        Receipt createdReceipt = receiptService.createUserReceipt(userId, date, categoryId, amount, reason, people, file);
//        return new ResponseEntity<>(createdReceipt, HttpStatus.CREATED);
//    }
//    
//
//    /**
//     * 영수증 수정
//     * PUT /receipts/{id}
//     */
//    @PutMapping("/{receiptId}")
//    public ResponseEntity<Receipt> updateReceipt(
//    		@PathVariable("receiptId") Integer id,
//            @RequestBody Receipt updated
//    ) {
//        Receipt result = receiptService.updateReceipt(id, updated);
//        return ResponseEntity.ok(result);
//    }
//    
//    
//    /**
//     * 영수증 승인/반려 상태 저장
//     * POST /receipts/user/{userId}/decisions
//     */
//    @PostMapping("/user/{userId}/decisions")
//    public ResponseEntity<Void> saveReceiptDecisions(
//            @PathVariable("userId") Integer userId,
//            @RequestBody List<ReceiptDecisionRequest> decisions
//    ) {
//        receiptService.saveReceiptDecisions(userId, decisions);
//        return ResponseEntity.ok().build();
//    }
//    
//
//    /**
//     * 영수증 삭제
//     * DELETE /receipts/{id}
//     */
//    @DeleteMapping("/{receiptId}")
//    public ResponseEntity<Void> deleteReceipt(@PathVariable("receiptId") Integer id) {
//        receiptService.deleteReceipt(id);
//        return ResponseEntity.noContent().build();
//    }
//}
