//package com.cs.rcpt.service;
//
//import com.cs.core.handler.GlobalExceptionHandler;
//import com.cs.core.util.FileNameUtils;
//import com.cs.rcpt.entity.Receipt;
//import com.cs.rcpt.entity.ReceiptAttachments;
//import com.cs.rcpt.entity.ReceiptCategory;
//import com.cs.rcpt.entity.ReceiptParticipants;
//import com.cs.rcpt.entity.ReceiptStatus;
//import com.cs.rcpt.repository.ReceiptCategoryRepository;
//import com.cs.rcpt.repository.ReceiptRepository;
//import com.cs.rcpt.repository.ReceiptStatusRepository;
//import com.cs.rcpt.service.reponse.ReceiptPage;
//import com.cs.rcpt.service.reponse.ReceiptYearlySummaryResponse;
//import com.cs.rcpt.service.request.ReceiptDecisionRequest;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.domain.*;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.net.URLEncoder;
//import java.nio.charset.StandardCharsets;
//import java.sql.Date;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class ReceiptService {
//
//    private final ReceiptRepository receiptRepository;
//    private final ReceiptStatusRepository receiptStatusRepository;
//    private final ReceiptCategoryRepository categoryRepository;
//    
//    // 파일 업로드 최대 개수/경로 등 (application.properties에서 주입)
//    @Value("${file.upload.total.size}")
//    private int totalSize;
//
//    @Value("${receipt.file.upload.path}")
//    private String receiptFileUploadPath;
//
//    @Value("${receipt.file.upload.url}")
//    private String receiptFileUploadUrl;
//    
//
//    /**
//     * (페이징) 모든 영수증 조회
//     * - 정렬 예시: Receipt_ID 내림차순
//     */
//    public ReceiptPage getReceipts(int page, int size, Sort sort) {
//        Pageable pageable = PageRequest.of(page, size, sort);
//
//        Page<Receipt> receiptPage = receiptRepository.findAll(pageable);
//
//        // Page 객체에서 필요한 정보 추출 후 DTO에 담아서 반환
//        return ReceiptPage.builder()
//                .content(receiptPage.getContent())
//                .totalPages(receiptPage.getTotalPages())
//                .totalElements(receiptPage.getTotalElements())
//                .pageNumber(receiptPage.getNumber())
//                .pageSize(receiptPage.getSize())
//                .build();
//    }
//
//    
//    public ReceiptPage getReceiptsByUserIdAndStatus(
//            Integer userId,
//            Integer statusCode,     // 1=대기, 2=승인, 3=반려
//            int page,
//            int size,
//            Sort sort
//    ) {
//        Pageable pageable = PageRequest.of(page, size, sort);
//
//        Page<Receipt> receiptPage =
//            receiptRepository.findByUserIdAndStatus_StatusCode(
//                userId, statusCode, pageable);
//
//        return toReceiptPage(receiptPage);      // 이미 있는 헬퍼 메서드
//    }
//
//    /**
//     * 사용자 영수증 페이징 조회
//     */
//    public ReceiptPage getReceiptsByUserId(Integer userId, int page, int size, Sort sort) {
//        Pageable pageable = PageRequest.of(page, size, sort);
//        Page<Receipt> receiptPage = receiptRepository.findByUserId(userId, pageable);
//
//        return new ReceiptPage(
//                receiptPage.getContent(),
//                receiptPage.getTotalPages(),
//                receiptPage.getTotalElements(),
//                receiptPage.getNumber(),
//                receiptPage.getSize()
//        );
//    }
//
//    
//    /**
//     * (비페이징) 모든 영수증 조회 - 필요시 남겨두거나 제거
//     */
//    public List<Receipt> getAllReceipts() {
//        return receiptRepository.findAll();
//    }
//
//    
//    /**
//     * 특정 영수증 조회
//     */
//    public Receipt getReceipt(Integer id) {
//        Optional<Receipt> opt = receiptRepository.findById(id);
//        return opt.orElse(null); 
//    }
//
//    
//    /**
//     * 사용자 영수증 생성 (multipart/form-data)
//     * 1) 날짜, 금액 등 폼 데이터 파싱
//     * 2) 상태코드=1 (신청)
//     * 3) 첨부파일 저장 → receipt_attachments
//     * 4) people JSON → receipt_participants
//     */
//    @Transactional
//    public Receipt createUserReceipt(
//            Integer userId,
//            String dateStr,
//            Integer categoryId,
//            String amountStr,
//            String reason,
//            String peopleJson,
//            MultipartFile file
//    ) {
//        // (1) Receipt 엔티티 생성
//        Receipt receipt = new Receipt();
//        receipt.setUserId(userId);
//
//        // 날짜(예: "2025-04-01") → java.sql.Date
//        if (dateStr != null && !dateStr.isEmpty()) {
//            receipt.setSubmissionDate(java.sql.Date.valueOf(dateStr));
//        }
//        
//        ReceiptCategory category = categoryRepository.findById(categoryId)
//        	.orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "카테고리 없음"));
//        
//        // category 필드에 type 저장
//        receipt.setCategory(category);
//
//        // amount (문자열 → BigDecimal)
//        if (amountStr != null && !amountStr.isEmpty()) {
//            receipt.setAmount(new BigDecimal(amountStr));
//        }
//
//        // 사유
//        receipt.setReason(reason);
//
//        // 상태: 코드=1 ("신청")
//        // DB에 status_code=1 레코드가 미리 "신청"으로 존재한다고 가정
//        ReceiptStatus status = receiptStatusRepository.findById(1)
//            .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "신청 상태(코드=1)가 없습니다."));
//        receipt.setStatus(status);
//
//        // (2) peopleJson → receipt_participants
//        // ex) [{"name":"권매니저","department":"경영관리본부","team":"경영팀"}]
//        if (peopleJson != null && !peopleJson.isEmpty()) {
//            List<ReceiptParticipants> participantsList = parseParticipants(peopleJson, receipt);
//            receipt.setParticipantsList(participantsList);
//        }
//
//        // (3) 첨부파일(단일)
//        if (file != null && !file.isEmpty()) {
//            // 첨부파일 개수 체크 (optional)
//            // if (1 > totalSize) { throw ... }
//
//            // 실제 파일 저장
//            // UUID + 원본파일명
//            String originalFilename = file.getOriginalFilename();
//            String safeFileName     = FileNameUtils.safeName(originalFilename);
//            
//            // 디스크 경로
//            File dest = new File(receiptFileUploadPath, safeFileName);
//            try {
//                file.transferTo(dest);
//            } catch (IOException e) {
//                throw new RuntimeException(GlobalExceptionHandler.CC + "파일 저장 실패", e);
//            }
//            
//            // DB 저장용 URL
//            String fileUrl = receiptFileUploadUrl + "/" + FileNameUtils.encodePathSegment(safeFileName);
//            
//            // ReceiptAttachments 엔티티 생성
//            ReceiptAttachments attachment = ReceiptAttachments.builder()
//                .fileUrl(fileUrl)
//                .fileType(file.getContentType())
//                .uploadDate(new java.sql.Date(System.currentTimeMillis()))
//                .fileOriginName(safeFileName) // 서버에 저장된 실제 파일명
//                .fileName(originalFilename)     // 사용자가 업로드한 원본 파일명
//                .build();
//
//            // 영수증과 연결 (1:1)
//            attachment.setReceipt(receipt);
//            receipt.setAttachment(attachment);
//        }
//
//        // (4) DB 저장
//        return receiptRepository.save(receipt);
//    }
//    
//    
//    /**
//     * 사용자의 영수증을 월별 필터까지 적용하여 조회
//     */
//    public ReceiptPage getReceiptByUserIdAndYearMonth(Integer userId, int page, int size, String yearMonth, Sort sort) {
//        Pageable pageable = PageRequest.of(page, size, sort);
//
//        // yearMonth가 없으면 기존 전체 조회
//        if (yearMonth == null || yearMonth.trim().isEmpty()) {
//            Page<Receipt> pageResult = receiptRepository.findByUserId(userId, pageable);
//            return toReceiptPage(pageResult);
//        }
//
//        // 예: "2025-04" → 연도=2025, 월=04
//        String[] arr = yearMonth.split("-");
//        if (arr.length < 2) {
//            // 형식이 잘못된 경우 전체 조회
//            Page<Receipt> pageResult = receiptRepository.findByUserId(userId, pageable);
//            return toReceiptPage(pageResult);
//        }
//        int year = Integer.parseInt(arr[0]);
//        int month = Integer.parseInt(arr[1]);
//
//        // 월별 시작일/종료일 계산 (LocalDate를 이용)
//        LocalDate start = LocalDate.of(year, month, 1);
//        LocalDate end = start.plusMonths(1).minusDays(1);  // 해당 월의 마지막 일
//
//        // java.sql.Date 객체로 변환
//        Date startDate = Date.valueOf(start);
//        Date endDate = Date.valueOf(end);
//
//        // 리포지토리 메서드 호출
//        Page<Receipt> filtered = receiptRepository.findByUserIdAndSubmissionDateBetween(userId, startDate, endDate, pageable);
//        return toReceiptPage(filtered);
//    }
//    
//    
//    /**
//     * 특정 사용자의 영수증 목록 (날짜 범위 필터, 상태 코드 있을시 조건문 추가) 페이징 조회
//     */
//    public ReceiptPage getReceiptsBySearchUserDateRange(
//            Integer userId,
//            String startDateStr,
//            String endDateStr,
//            Integer statusCode,
//            int page,
//            int size,
//            Sort sort
//    ) {
//        Pageable pageable = PageRequest.of(page, size, sort);
//
//        // 시작일/종료일 기본 처리
//        if (startDateStr == null || startDateStr.trim().isEmpty()) {
//            startDateStr = LocalDate.now().toString();
//        }
//        if (endDateStr == null || endDateStr.trim().isEmpty()) {
//            endDateStr = LocalDate.now().toString();
//        }
//        Date startDate = Date.valueOf(startDateStr);
//        Date endDate = Date.valueOf(endDateStr);
//
//        // Repository 동적 메서드 호출 (statusCode가 null이면 상태 필터 없이 조회됨)
//        Page<Receipt> receiptPage = receiptRepository.findReceiptDynamic(userId, startDate, endDate, statusCode, pageable);
//
//        return toReceiptPage(receiptPage);
//    }
//    
//    
//    /**
//     * (페이징) 전체 사용자 영수증 조회 - 월별 필터 (유저 아이디 없이)
//     * 예) GET /receipts/with-status/all?yearMonth=2025-04&page=0&size=10
//     */
//    public ReceiptPage getReceiptsByYearMonthForAll(int page, int size, String yearMonth, Sort sort) {
//        Pageable pageable = PageRequest.of(page, size, sort);
//        if (yearMonth == null || yearMonth.trim().isEmpty()) {
//            Page<Receipt> pageResult = receiptRepository.findAll(pageable);
//            return toReceiptPage(pageResult);
//        }
//        
//        String[] arr = yearMonth.split("-");
//        if (arr.length < 2) {
//            Page<Receipt> pageResult = receiptRepository.findAll(pageable);
//            return toReceiptPage(pageResult);
//        }
//        
//        int year = Integer.parseInt(arr[0]);
//        int month = Integer.parseInt(arr[1]);
//        
//        LocalDate start = LocalDate.of(year, month, 1);
//        LocalDate end = start.plusMonths(1).minusDays(1); // 해당 월의 마지막 일
//        Date startDate = Date.valueOf(start);
//        Date endDate = Date.valueOf(end);
//        
//        // 전체 영수증에서 제출일이 startDate와 endDate 사이인 경우 조회
//        Page<Receipt> filtered = receiptRepository.findBySubmissionDateBetween(startDate, endDate, pageable);
//        
//        return toReceiptPage(filtered);
//    }
//    
//    
//    /**
//     * 사용자(userId)의 특정 연도(year)에 대해
//     * 월별로 (requested, approved, rejected, total)을 합산하여 반환
//     */
//    public ReceiptYearlySummaryResponse getReceiptByUserYearlySummary(Integer userId, int year) {
//        // 1) DB에서 해당 연도의 (월, 상태, 금액 합계) 정보를 가져온다
//        //    → Repository 커스텀쿼리 or NativeQuery 등으로 group by month, status
//        List<Object[]> rawList = receiptRepository.findYearlySummaryByUserId(userId, year);
//        // rawList는 예:  [[3, 1, 30000], [3,2,20000], [4,1,50000], ...]
//
//        // 2) month 별로 requested/approved/rejected/total 누적
//        //    월: 1..12까지 돌면서, DB 결과를 매핑
//        long totalRequested = 0;	// 신청
//        long totalApproved = 0;		// 승인
//        long totalRejected = 0;		// 반려
//        long total = 0;				// 합계
//
//        // 월별 결과를 저장할 리스트
//        List<ReceiptYearlySummaryResponse.MonthlySummary> monthlyList = new ArrayList<>();
//        // 1월~12월 초기화
//        for(int m=1; m<=12; m++){
//            ReceiptYearlySummaryResponse.MonthlySummary ms = ReceiptYearlySummaryResponse.MonthlySummary.builder()
//                .month(m)
//                .requested(0)
//                .approved(0)
//                .rejected(0)
//                .sum(0)
//                .build();
//            monthlyList.add(ms);
//        }
//
//        // rawList를 순회하여, month & statusCode 별 금액 합을 monthlyList에 반영
//        // 예: rawList 원소: [ monthValue, statusCode, sumAmount ]
//        for(Object[] row : rawList){
//            int monthValue = ((Number)row[0]).intValue();
//            int statusCode = ((Number)row[1]).intValue();
//            long sumAmount = ((Number)row[2]).longValue();
//
//            // monthlyList에서 monthValue-1 인덱스에 접근
//            ReceiptYearlySummaryResponse.MonthlySummary ms = monthlyList.get(monthValue - 1);
//
//            // statusCode=1(신청),2(승인),3(반려)
//            if(statusCode == 1) {
//                ms.setRequested(ms.getRequested() + sumAmount);
//            } else if(statusCode == 2) {
//                ms.setApproved(ms.getApproved() + sumAmount);
//            } else if(statusCode == 3) {
//                ms.setRejected(ms.getRejected() + sumAmount);
//            }
//            ms.setSum(ms.getSum() + sumAmount);
//        }
//
//        // 각 월별 합을 모두 더해 totalRequested, totalApproved 등 계산
//        for(ReceiptYearlySummaryResponse.MonthlySummary ms : monthlyList) {
//            totalRequested += ms.getRequested();
//            totalApproved += ms.getApproved();
//            totalRejected += ms.getRejected();
//            total += ms.getSum();
//        }
//
//        // 3) DTO 생성
//        ReceiptYearlySummaryResponse response = ReceiptYearlySummaryResponse.builder()
//            .year(year)
//            .monthlyList(monthlyList)
//            .totalRequested(totalRequested)
//            .totalApproved(totalApproved)
//            .totalRejected(totalRejected)
//            .totalSum(total)
//            .build();
//
//        return response;
//    }
//    
//    
//    /**
//     * ReceiptPage 변환 메서드
//     */
//    private ReceiptPage toReceiptPage(Page<Receipt> pageResult) {
//        return ReceiptPage.builder()
//                .content(pageResult.getContent())
//                .totalPages(pageResult.getTotalPages())
//                .totalElements(pageResult.getTotalElements())
//                .pageNumber(pageResult.getNumber())
//                .pageSize(pageResult.getSize())
//                .build();
//    }
//
//    
//    /**
//     * 영수증 수정
//     */
//    public Receipt updateReceipt(Integer id, Receipt updated) {
//        return receiptRepository.findById(id)
//            .map(original -> {
//                original.setUserId(updated.getUserId());
//                original.setSubmissionDate(updated.getSubmissionDate());
//                original.setCategory(updated.getCategory());
//                original.setReason(updated.getReason());
//                original.setAmount(updated.getAmount());
//                original.setLastApprovalDate(updated.getLastApprovalDate());
//                original.setLastRejectionDate(updated.getLastRejectionDate());
//                original.setStatus(updated.getStatus());
//                original.setAttachment(updated.getAttachment());
//                original.setIssuingLocation(updated.getIssuingLocation());
//                // 필요하다면 ParticipantsList도 갱신
//                return receiptRepository.save(original);
//            })
//            .orElseGet(() -> receiptRepository.save(updated));
//    }
//    
//    
//
//    @Transactional
//    public void saveReceiptDecisions(Integer userId, List<ReceiptDecisionRequest> decisions) {
//        for (ReceiptDecisionRequest dto : decisions) {
//            Receipt receipt = receiptRepository.findById(dto.getReceiptId())
//                .orElseThrow(() -> new IllegalArgumentException(GlobalExceptionHandler.CC + "Receipt not found: " + dto.getReceiptId()));
//
//            // 해당 사용자의 영수증인지 확인
//            if (!receipt.getUserId().equals(userId)) {
//                throw new IllegalArgumentException(GlobalExceptionHandler.CC + "User does not own receipt: " + dto.getReceiptId());
//            }
//
//            // 상태코드에 따라 ReceiptStatus를 찾음 (1=신청, 2=승인, 3=반려)
//            ReceiptStatus newStatus = receiptStatusRepository.findById(dto.getStatusCode())
//                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "해당 코드의 상태가 존재하지 않습니다. code=" + dto.getStatusCode()));
//
//            receipt.setStatus(newStatus);
//
//            // 승인(2)인 경우
//            if (dto.getStatusCode() == 2) {
//            	receipt.setLastApproverId(dto.getApproverId());
//            	receipt.setLastApproverName(dto.getApproverName());
//                receipt.setLastApprovalDate(new java.sql.Date(System.currentTimeMillis()));
//                receipt.setLastRejectionDate(null);
//                // 승인 시 반려사유 초기화
//                receipt.setReason(null);
//            }
//            // 반려(3)인 경우
//            else if (dto.getStatusCode() == 3) {
//            	receipt.setLastApproverId(dto.getApproverId());
//            	receipt.setLastApproverName(dto.getApproverName());
//                receipt.setLastRejectionDate(new java.sql.Date(System.currentTimeMillis()));
//                // 반려사유
//                receipt.setReason(dto.getRejectReason());
//                receipt.setLastApprovalDate(null);
//            }
//            // 신청(1)인 경우
//            else if (dto.getStatusCode() == 1) {
//            	receipt.setLastApproverId(dto.getApproverId());
//            	receipt.setLastApproverName(dto.getApproverName()+"(복구)");
//                // 신청 상태로 되돌릴 때 반려일/승인일/반려사유 모두 제거
//                receipt.setLastRejectionDate(null);
//                receipt.setLastApprovalDate(null);
//                receipt.setReason(null);
//            }
//
//            receiptRepository.save(receipt);
//        }
//    }
//
//    
//    /**
//     * 영수증 삭제
//     */
//    public void deleteReceipt(Integer id) {
//        receiptRepository.deleteById(id);
//    }
//    
//
//    /**
//     * people JSON 파싱 → receipt_participants
//     *   예: "[{"name":"권매니저","department":"경영관리본부","team":"경영팀"}]"
//     *   -> List<ReceiptParticipants>
//     */
//    private List<ReceiptParticipants> parseParticipants(String peopleJson, Receipt parent) {
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            List<Map<String,Object>> arr = mapper.readValue(peopleJson, new TypeReference<>(){});
//            List<ReceiptParticipants> result = new ArrayList<>();
//            for (Map<String,Object> p : arr) {
//                ReceiptParticipants rp = new ReceiptParticipants();
//                
//                rp.setParticipantUserId((Integer)p.get("userId"));
//                rp.setParticipantName((String)p.get("name"));
//                rp.setDepartment((String)p.get("department"));
//                rp.setTeam((String)p.get("team"));
//
//                rp.setReceipt(parent); // 양방향 매핑 (N:1)
//                result.add(rp);
//            }
//            return result;
//        } catch (IOException e) {
//            throw new RuntimeException(GlobalExceptionHandler.CC + "participants JSON 파싱 실패", e);
//        }
//    }
//    
//    
//    /**
//     * ReceiptStatus 전체 목록 조회
//     */
//    public List<ReceiptStatus> getStatuses() {
//        return receiptStatusRepository.findAll();
//    }
//    
//    
//    private static String encode(String s) {
//        return URLEncoder.encode(s, StandardCharsets.UTF_8)
//                         .replace("+", "%20");
//    }
//}


