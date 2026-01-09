package com.cs.rcpt.service;

import com.cs.rcpt.controller.response.ReceiptApprClosedSummaryDto;
import com.cs.rcpt.controller.response.ReceiptHistoryOverviewDto;
import com.cs.rcpt.controller.response.ReceiptPage;
import com.cs.rcpt.controller.response.ReceiptUserSummaryDto;
import com.cs.rcpt.entity.Receipt;
import com.cs.rcpt.enums.ReceiptProcessStatus;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 영수증 검색 서비스 인터페이스
 */
public interface ReceiptSearchService {

    /**
     * 내 승인 대상 영수증 요약 조회
     * @param approverId 결재자 ID
     * @param userId 신청자 ID
     * @param start 시작 날짜
     * @param end 종료 날짜
     * @param statuses 상태 목록
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 영수증 사용자 요약 페이지
     */
    ReceiptPage<ReceiptUserSummaryDto> getSummaryOnlyForMe(Integer approverId,
                                                             Integer userId,
                                                             LocalDate start,
                                                             LocalDate end,
                                                             List<ReceiptProcessStatus> statuses,
                                                             int page, int size);

    /**
     * 전역 통계 조회
     * @param s 시작 날짜
     * @param e 종료 날짜
     * @param approverId 결재자 ID
     * @param userId 신청자 ID
     * @param statuses 상태 목록
     * @return 통계 맵
     */
    Map<String, Object> getGlobalTotals(LocalDate s, LocalDate e,
                                        Integer approverId,
                                        Integer userId,
                                        List<ReceiptProcessStatus> statuses);

    /**
     * (페이징) '내 결재선' 영수증 조회
     * @param approverId 결재자 ID
     * @param userId 신청자 ID
     * @param startStr 시작 날짜 문자열
     * @param endStr 종료 날짜 문자열
     * @param statuses 상태 목록
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 기준
     * @return 영수증 페이지
     */
    ReceiptPage<Receipt> getMyPendingByDate(Integer approverId,
                                            Integer userId,
                                            String startStr,
                                            String endStr,
                                            List<ReceiptProcessStatus> statuses,
                                            int page,
                                            int size,
                                            Sort sort);

    /**
     * (페이징) '내 차례' + Role(1,2,3) 전용
     * @param approverId 결재자 ID
     * @param userId 신청자 ID
     * @param startStr 시작 날짜 문자열
     * @param endStr 종료 날짜 문자열
     * @param roles 역할 목록
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 기준
     * @return 영수증 페이지
     */
    ReceiptPage<Receipt> getMyPendingByDateRoles(Integer approverId, Integer userId,
                                                 String startStr, String endStr,
                                                 List<Integer> roles,
                                                 int page, int size, Sort sort);

    /**
     * (페이징) 위임(대리결재자) 관점 '내 차례' + 결재/합의(1·2·3)
     * @param delegateId 대리자 ID
     * @param userId 신청자 ID
     * @param startStr 시작 날짜 문자열
     * @param endStr 종료 날짜 문자열
     * @param roles 역할 목록
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 기준
     * @return 영수증 페이지
     */
    ReceiptPage<Receipt> getMyPendingByDateRolesAsDelegate(Integer delegateId, Integer userId,
                                                            String startStr, String endStr,
                                                            List<Integer> roles,
                                                            int page, int size, Sort sort);

    /**
     * 사용자별 날짜 범위 영수증 조회
     * @param userId 신청자 ID
     * @param startStr 시작 날짜 문자열
     * @param endStr 종료 날짜 문자열
     * @param statuses 상태 목록
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 영수증 페이지
     */
    ReceiptPage<Receipt> getByUserAndDate(Integer userId,
                                          String startStr,
                                          String endStr,
                                          List<ReceiptProcessStatus> statuses,
                                          int page, int size);

    /**
     * (페이징) 위임(대리결재자) 관점 전체 상태 조회
     * @param delegateId 대리자 ID
     * @param userId 신청자 ID
     * @param startStr 시작 날짜 문자열
     * @param endStr 종료 날짜 문자열
     * @param statuses 상태 목록
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 기준
     * @return 영수증 페이지
     */
    ReceiptPage<Receipt> getByDelegateAndDateRange(Integer delegateId, Integer userId,
                                                    String startStr, String endStr,
                                                    List<ReceiptProcessStatus> statuses,
                                                    int page, int size, Sort sort);

    /**
     * (요약) 위임(대리결재자) 관점 신청자별 집계 – 전체 상태
     * @param delegateId 대리자 ID
     * @param userId 신청자 ID
     * @param start 시작 날짜
     * @param end 종료 날짜
     * @param statuses 상태 목록
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 영수증 사용자 요약 페이지
     */
    ReceiptPage<ReceiptUserSummaryDto> getSummaryForDelegate(Integer delegateId, Integer userId,
                                                              LocalDate start, LocalDate end,
                                                              List<ReceiptProcessStatus> statuses,
                                                              int page, int size);

    /**
     * 내 차례 영수증 사용자별 요약 조회
     * @param approverId 결재자 ID
     * @param userId 신청자 ID
     * @param departmentId 부서 ID
     * @param teamId 팀 ID
     * @param start 시작 날짜
     * @param end 종료 날짜
     * @param roles 역할 목록
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 영수증 사용자 요약 페이지
     */
    ReceiptPage<ReceiptUserSummaryDto> getPendingSummaryByUser(Integer approverId, Integer userId,
                                                               Integer departmentId, Integer teamId,
                                                               LocalDate start, LocalDate end,
                                                               List<Integer> roles,
                                                               int page, int size);

    /**
     * (요약) 위임(대리결재자) 관점 '내 차례' – 신청자별 집계
     * @param delegateId 대리자 ID
     * @param userId 신청자 ID
     * @param start 시작 날짜
     * @param end 종료 날짜
     * @param roles 역할 목록
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 영수증 사용자 요약 페이지
     */
    ReceiptPage<ReceiptUserSummaryDto> getPendingSummaryByUserAsDelegate(Integer delegateId, Integer userId,
                                                                         LocalDate start, LocalDate end,
                                                                         List<Integer> roles,
                                                                         int page, int size);

    /**
     * 영수증 신청 내역 조회 통계
     * @param approverId 결재자 ID
     * @param searchUserId 검색 대상 사용자 ID
     * @param startStr 시작 날짜 문자열
     * @param endStr 종료 날짜 문자열
     * @param status 상태
     * @return 통계 맵
     */
    Map<String, Long> getTotalsForMyPending(Integer approverId,
                                            Integer searchUserId,
                                            String startStr,
                                            String endStr,
                                            ReceiptProcessStatus status);

    /**
     * 최근 활동(간단) – 영수증 도메인 이벤트 기준(제출/승인/반려/마감 등)
     * @param approverId 결재자 ID
     * @param startStr 시작 날짜 문자열
     * @param endStr 종료 날짜 문자열
     * @param limit 제한 개수
     * @return 활동 목록
     */
    List<ReceiptHistoryOverviewDto.Activity> findRecentActivitiesForApprover(Integer approverId, String startStr, String endStr, Integer limit);

    /**
     * 개인 화면용 히스토리 개요를 생성
     * @param approverId 결재자 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param limit 제한 개수
     * @return 히스토리 개요 DTO
     */
    ReceiptHistoryOverviewDto buildHistoryOverview(Integer approverId, String startDate, String endDate, int limit);

    /**
     * 내 차례 영수증 통계 조회 (역할별)
     * @param approverId 결재자 ID
     * @param searchUserId 검색 대상 사용자 ID
     * @param departmentId 부서 ID
     * @param teamId 팀 ID
     * @param startStr 시작 날짜 문자열
     * @param endStr 종료 날짜 문자열
     * @param roles 역할 목록
     * @return 통계 맵
     */
    Map<String, Long> getTotalsForMyPendingRoles(Integer approverId, Integer searchUserId,
                                                   Integer departmentId, Integer teamId,
                                                   String startStr, String endStr,
                                                   List<Integer> roles);

    /**
     * 승인/마감 요약 조회
     * @param approverId 결재자 ID
     * @param userId 신청자 ID
     * @param start 시작 날짜
     * @param end 종료 날짜
     * @param statuses 상태 목록
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 승인/마감 요약 페이지
     */
    ReceiptPage<ReceiptApprClosedSummaryDto> getApprClosedSummary(Integer approverId, Integer userId,
                                                                   LocalDate start, LocalDate end,
                                                                   List<ReceiptProcessStatus> statuses,
                                                                   int page, int size);

    /**
     * 선택된 월(YYYY-MM)을 기준으로, 그 이전 월들 중 '승인' 상태의 영수증이 있는 월 목록을 조회
     * @param currentMonth YYYY-MM 형식의 현재 조회 월
     * @return YYYY-MM 형식의 과거 승인 월 목록
     */
    List<String> findPastApprovedMonths(String currentMonth);

    /**
     * 사용자 비활성화 및 권한 변경으로 인한 영수증 결재선 목록 조회 (페이징)
     * @param approverId 결재자 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 페이징된 영수증 목록 DTO
     */
    ReceiptPage<Receipt> getMyPendingApprovals(Integer approverId, int page, int size);

    /**
     * 역할별 대기 건수 조회
     * @param approverId 결재자 ID
     * @return 대기 건수 맵
     */
    Map<String, Object> getPendingCountsByRole(Integer approverId);
}
