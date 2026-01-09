package com.cs.rcpt.service;

import com.cs.rcpt.controller.response.ReceiptPage;
import com.cs.rcpt.controller.response.ReceiptYearlySummaryResponse;
import com.cs.rcpt.entity.Receipt;
import com.cs.rcpt.enums.ReceiptProcessStatus;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 영수증 서비스 인터페이스
 */
public interface ReceiptService {

    /**
     * (페이징) 전체 영수증 조회
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 기준
     * @return 영수증 페이지
     */
    ReceiptPage<Receipt> getReceipts(int page, int size, Sort sort);

    /**
     * (페이징) userId + statusCode 조건 조회
     * @param userId 사용자 ID
     * @param status 상태
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 기준
     * @return 영수증 페이지
     */
    ReceiptPage<Receipt> getReceiptsByUserIdAndStatus(Integer userId,
                                                      ReceiptProcessStatus status,
                                                      int page, int size,
                                                      Sort sort);

    /**
     * (페이징) userId + 다중 statusCode 조건 조회
     * @param userId 사용자 ID
     * @param statuses 상태 목록
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 기준
     * @return 영수증 페이지
     */
    ReceiptPage<Receipt> getReceiptsByUserIdAndStatuses(Integer userId, List<ReceiptProcessStatus> statuses, int page, int size,
                                                         Sort sort);

    /**
     * (페이징) userId 전부 조회
     * @param userId 사용자 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 기준
     * @return 영수증 페이지
     */
    ReceiptPage<Receipt> getReceiptsByUserId(Integer userId,
                                              int page, int size,
                                              Sort sort);

    /**
     * 영수증 단건 조회
     * @param id 영수증 ID
     * @return 영수증 엔티티
     */
    Receipt getReceipt(Integer id);

    /**
     * 사용자 영수증 생성 (multipart/form-data)
     * @param userId 사용자 ID
     * @param userName 사용자 이름
     * @param userEmail 사용자 이메일
     * @param dateStr 날짜 문자열
     * @param categoryId 카테고리 ID
     * @param amountStr 금액 문자열
     * @param reason 사유
     * @param participantsJson 참여자 JSON
     * @param approversJson 결재자 JSON
     * @param departmentId 부서 ID
     * @param departmentName 부서 이름
     * @param teamId 팀 ID
     * @param teamName 팀 이름
     * @param file 첨부파일
     * @return 생성된 영수증 엔티티
     */
    Receipt createUserReceipt(Integer userId,
                              String userName,
                              String userEmail,
                              String dateStr,
                              Integer categoryId,
                              String amountStr,
                              String reason,
                              String participantsJson,
                              String approversJson,
                              Integer departmentId,
                              String departmentName,
                              Integer teamId,
                              String teamName,
                              MultipartFile file);

    /**
     * 사용자별 연월별 영수증 조회
     * @param userId 사용자 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param yearMonth 연월 (YYYY-MM)
     * @param sort 정렬 기준
     * @return 영수증 페이지
     */
    ReceiptPage<Receipt> getReceiptByUserIdAndYearMonth(Integer userId,
                                                         int page, int size,
                                                         String yearMonth,
                                                         Sort sort);

    /**
     * 사용자별 날짜 범위 영수증 조회
     * @param userId 사용자 ID
     * @param startDateStr 시작 날짜 문자열
     * @param endDateStr 종료 날짜 문자열
     * @param status 상태
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 기준
     * @return 영수증 페이지
     */
    ReceiptPage<Receipt> getReceiptsBySearchUserDateRange(Integer userId,
                                                           String startDateStr,
                                                           String endDateStr,
                                                           ReceiptProcessStatus status,
                                                           int page, int size,
                                                           Sort sort);

    /**
     * 전체 사용자 연월별 영수증 조회
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param yearMonth 연월 (YYYY-MM)
     * @param sort 정렬 기준
     * @return 영수증 페이지
     */
    ReceiptPage<Receipt> getReceiptsByYearMonthForAll(int page, int size,
                                                       String yearMonth,
                                                       Sort sort);

    /**
     * 사용자별 연도별 영수증 요약 조회
     * @param userId 사용자 ID
     * @param year 연도
     * @return 연도별 요약 응답
     */
    ReceiptYearlySummaryResponse getReceiptByUserYearlySummary(Integer userId, int year);

    /**
     * 영수증 수정
     * @param id 영수증 ID
     * @param updated 수정할 영수증 엔티티
     * @return 수정된 영수증 엔티티
     */
    Receipt updateReceipt(Integer id, Receipt updated);

    /**
     * 영수증 부분 수정(patch)
     * @param userId 사용자 ID
     * @param receiptId 영수증 ID
     * @param dateStr 날짜 문자열
     * @param categoryId 카테고리 ID
     * @param amountStr 금액 문자열
     * @param reason 사유
     * @param participantsJson 참여자 JSON
     * @param approversJson 결재자 JSON
     * @param newFile 새 첨부파일
     * @param deleteFile 파일 삭제 여부
     * @return 수정된 영수증 엔티티
     */
    Receipt patchReceipt(Integer userId, Integer receiptId,
                         String dateStr, Integer categoryId, String amountStr, String reason,
                         String participantsJson, String approversJson,
                         MultipartFile newFile, Boolean deleteFile);

    /**
     * 영수증 삭제
     * @param id 영수증 ID
     */
    void deleteReceipt(Integer id);
}
