package com.cs.rcpt.service.impl;

import com.cs.rcpt.controller.response.ReceiptApprClosedSummaryDto;
import com.cs.rcpt.controller.response.ReceiptHistoryOverviewDto;
import com.cs.rcpt.controller.response.ReceiptPage;
import com.cs.rcpt.controller.response.ReceiptUserSummaryDto;
import com.cs.rcpt.entity.Receipt;
import com.cs.rcpt.enums.ReceiptProcessStatus;
import com.cs.rcpt.repository.ReceiptRepository;
import com.cs.rcpt.repository.ReceiptSearchRepository;
import com.cs.rcpt.service.ReceiptSearchService;
import com.cs.rcpt.utils.ReceiptConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 영수증 검색 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class ReceiptSearchServiceImpl implements ReceiptSearchService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptSearchRepository receiptSearchRepository;
    private final int DEFAULT_RECENT_LIMIT = 8;

    @Override
    @Transactional
    public ReceiptPage<ReceiptUserSummaryDto> getSummaryOnlyForMe(Integer approverId,
	            Integer userId,
	            LocalDate start,
	            LocalDate end,
	            List<ReceiptProcessStatus> statuses,
	            int page, int size) {
	
		Pageable pageable = PageRequest.of(page, size, Sort.by("userId"));
		
		Page<Object[]> raw = receiptRepository.findSummaryForMyApproval(
		approverId, userId,
		Date.valueOf(start), Date.valueOf(end),
		statuses,
		pageable);
		
		return toReceiptPage(raw);   // ✨ 헬퍼로 변환
	}

    @Override
    @Transactional
    public Map<String,Object> getGlobalTotals(LocalDate s, LocalDate e,
								              Integer approverId,
								              Integer userId,
								              List<ReceiptProcessStatus> statuses) {

			Date start = Date.valueOf(s);
			Date end   = Date.valueOf(e);
			
			Object[] row = receiptRepository
			.findTotalsDynamic(start, end, approverId, userId, statuses)
			.get(0);                     // 항상 1 행
			
			return Map.of(
				"totalCnt", row[0],
				"waiting" , row[1],
				"request" , row[2],
				"approved", row[3],
				"rejected", row[4],
				"closed"  , row[5]
			);
	}



    /**
     * (페이징) '내 결재선' 영수증 조회
     *   └ status == null            → 전체(REQUEST + APPROVED + REJECTED)
     *   └ status == REQUEST         → 아직 내 차례(PENDING)만
     *   └ status == APPROVED/REJECTED→ 해당 상태만
     */
    @Override
    @Transactional
    public ReceiptPage<Receipt> getMyPendingByDate(Integer approverId,
                                                   Integer userId,
                                                   String   startStr,
                                                   String   endStr,
                                                   List<ReceiptProcessStatus> statuses,
                                                   int      page,
                                                   int      size,
                                                   Sort     sort) {

        /* ① 조회 기간 계산 ------------------------------------------------------------- */
        Date start = Date.valueOf(
                (startStr == null || startStr.isBlank())
                        ? LocalDate.now()
                        : LocalDate.parse(startStr));

        Date end   = Date.valueOf(
                (endStr == null || endStr.isBlank())
                        ? LocalDate.now()
                        : LocalDate.parse(endStr));

        Pageable pageable = PageRequest.of(page, size, sort);

        /* ② 상태별 Repository 호출 분기 ---------------------------------------------- */
        Page<Receipt> pg;

        if (statuses == null || statuses.isEmpty()) {
            // 전체
            pg = receiptRepository.findByApproverAndDateRange(
                    approverId, userId, start, end, null, pageable);

        } else if (statuses.size() == 1 &&
        		   statuses.get(0) == ReceiptProcessStatus.REQUEST) {
            // 아직 '내 차례' 건만
            pg = receiptRepository.findPendingApprovalsOnly(
                    approverId, userId, start, end, pageable);

        } else {
        	// APPROVED / REJECTED / CLOSED … (다중 허용)
            pg = receiptRepository.findByApproverAndDateRange(
                    approverId, userId, start, end, statuses, pageable);
        }

        /* ③ Page<Receipt> → ReceiptPage<Receipt> ------------------------------------- */
        return ReceiptConverter.toReceiptPage(pg);
    }

    
    /**
     * (페이징) '내 차례' + Role(1,2,3) 전용
     */
    @Override
    @Transactional
    public ReceiptPage<Receipt> getMyPendingByDateRoles(
            Integer approverId, Integer userId,
            String startStr, String endStr,
            List<Integer> roles,               // ②
            int page, int size, Sort sort) {

        Date start = Date.valueOf(startStr);
        Date end   = Date.valueOf(endStr);
        Pageable pageable = PageRequest.of(page, size, sort);

        /* 파라미터 없으면 결재(1)만 기본값 */
        if (roles == null || roles.isEmpty()) roles = List.of(1);

        Page<Receipt> pg =
            receiptRepository.findPendingApprovalsByRoles(
                approverId, userId, start, end, roles, pageable);

        return ReceiptConverter.toReceiptPage(pg);
    }
    
    /**
     * (페이징) 위임(대리결재자) 관점 '내 차례' + 결재/합의(1·2·3)
     */
    @Override
    @Transactional
    public ReceiptPage<Receipt> getMyPendingByDateRolesAsDelegate(
            Integer delegateId, Integer userId,
            String startStr, String endStr,
            List<Integer> roles,
            int page, int size, Sort sort) {

        Date start = Date.valueOf(startStr);
        Date end   = Date.valueOf(endStr);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Receipt> pg = receiptSearchRepository.findPendingApprovalsByDelegateRoles(
                delegateId, userId, start, end, roles, LocalDateTime.now(), pageable);

        return ReceiptConverter.toReceiptPage(pg);
    }
    
    
    @Override
    @Transactional
    public ReceiptPage<Receipt> getByUserAndDate(Integer userId,
                                                 String startStr,
                                                 String endStr,
                                                 List<ReceiptProcessStatus> statuses,
                                                 int page, int size) {

        Date start = Date.valueOf(startStr);
        Date end   = Date.valueOf(endStr);
        Pageable pageable = PageRequest.of(page, size,
                           Sort.by(Sort.Order.desc("submissionDate"),
                                   Sort.Order.desc("receiptId")));

        Page<Receipt> pg = receiptRepository.findByUserAndDateRange(
                userId, start, end, statuses, pageable);

        return ReceiptConverter.toReceiptPage(pg);
    }

    /**
     * (페이징) 위임(대리결재자) 관점 전체 상태 조회
     * - REQUEST 한정이 아닌 WAITING/REQUEST/APPROVED/REJECTED/CLOSED 전체
     */
    @Override
    @Transactional
    public ReceiptPage<Receipt> getByDelegateAndDateRange(
            Integer delegateId, Integer userId,
            String startStr, String endStr,
            List<ReceiptProcessStatus> statuses,
            int page, int size, Sort sort) {

        Date start = Date.valueOf(startStr);
        Date end   = Date.valueOf(endStr);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Receipt> pg = receiptSearchRepository.findByDelegateAndDateRange(
                delegateId, userId, start, end, statuses, LocalDateTime.now(), pageable);

        return ReceiptConverter.toReceiptPage(pg);
    }

    /**
     * (요약) 위임(대리결재자) 관점 신청자별 집계 – 전체 상태
     */
    @Override
    @Transactional
    public ReceiptPage<ReceiptUserSummaryDto> getSummaryForDelegate(
            Integer delegateId, Integer userId,
            LocalDate start, LocalDate end,
            List<ReceiptProcessStatus> statuses,
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("userName"));
        Page<Object[]> raw = receiptSearchRepository.findSummaryForDelegateApproval(
                delegateId, userId, Date.valueOf(start), Date.valueOf(end), statuses, LocalDateTime.now(), pageable);

        return toReceiptPage(raw);
    }
    
    
    // ---------- 여기에 추가 ----------
    /** Object[] -> DTO 변환 : 13컬럼(전체 집계) / 9컬럼(내-차례 집계) 모두 지원 */
    private List<ReceiptUserSummaryDto> convertRawSummary(List<Object[]> rows) {

        return rows.stream().map(r -> {
            int len = r.length;              // 9 or 13

            ReceiptUserSummaryDto.ReceiptUserSummaryDtoBuilder b = ReceiptUserSummaryDto.builder()
                .userId         ((Integer) r[0])
                .userName       ((String)  r[1])
                .userEmail      ((String)  r[2])
                .departmentId   ((Integer) r[3])
                .departmentName ((String)  r[4])
                .teamId         ((Integer) r[5])
                .teamName       ((String)  r[6])
                .count          (((Number)  r[7]).longValue());

            /* ─── 결과 행이 13칸(전체 집계) ─────────────────────────────── */
            if (len >= 13) {
                b.waiting   (((Number) r[8 ]).longValue())
                 .requested (((Number) r[9 ]).longValue())
                 .approved  (((Number) r[10]).longValue())
                 .rejected  (((Number) r[11]).longValue())
                 .closed    (((Number) r[12]).longValue());
            }
            /* ─── 결과 행이 9칸(내-차례 집계) ─────────────────────────── */
            else { // len == 9
                b.waiting   (0L)
                 .requested (((Number) r[8]).longValue())
                 .approved  (0L)
                 .rejected  (0L)
                 .closed    (0L);
            }
            return b.build();
        }).toList();
    }

    /** Page<Object[]> -> ReceiptPage<DTO> 로 래핑 */
    private ReceiptPage<ReceiptUserSummaryDto> toReceiptPage(Page<Object[]> pg) {
        List<ReceiptUserSummaryDto> dtoList = convertRawSummary(pg.getContent());

        return ReceiptPage.<ReceiptUserSummaryDto>builder()
                .content(dtoList)
                .totalPages(pg.getTotalPages())
                .totalElements(pg.getTotalElements())
                .pageNumber(pg.getNumber())
                .pageSize(pg.getSize())
                .build();
    }
    
    @Override
    @Transactional
    public ReceiptPage<ReceiptUserSummaryDto> getPendingSummaryByUser(
            Integer approverId, Integer userId,
            Integer departmentId, Integer teamId,
            LocalDate start, LocalDate end,
            List<Integer> roles,
            int page, int size)
    {
        if (roles == null || roles.isEmpty()) roles = List.of(1);   // 기본 결재(1)

        Pageable pageable = PageRequest.of(page, size, Sort.by("userName"));
        Page<Object[]> raw = receiptRepository.findPendingSummaryByUser(
                approverId, userId, departmentId, teamId,
                Date.valueOf(start), Date.valueOf(end),
                roles, pageable);

        /* ─ Object[] → DTO 재사용 ─ */
        return toReceiptPage(raw);   // convertRawSummary() 그대로 활용 가능
    }
    
    /**
     * (요약) 위임(대리결재자) 관점 '내 차례' – 신청자별 집계
     */
    @Override
    @Transactional
    public ReceiptPage<ReceiptUserSummaryDto> getPendingSummaryByUserAsDelegate(
            Integer delegateId, Integer userId,
            LocalDate start, LocalDate end,
            List<Integer> roles,
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("userName"));
        Page<Object[]> raw = receiptSearchRepository.findPendingSummaryByUserAsDelegate(
                delegateId, userId, Date.valueOf(start), Date.valueOf(end), roles, LocalDateTime.now(), pageable);

        return toReceiptPage(raw);
    }
    
	/**
	 * 영수증 신청 내역 조회 통계
	 */
    @Override
    @Transactional
    public Map<String, Long> getTotalsForMyPending(Integer approverId,
                                                   Integer searchUserId,
                                                   String   startStr,
                                                   String   endStr,
                                                   ReceiptProcessStatus status) {

        Date start = Date.valueOf(startStr);
        Date end   = Date.valueOf(endStr);

        List<Object[]> rows = receiptRepository.findTotalsDynamicForApprover(
                approverId, searchUserId, start, end, status);

        long cnt   = 0;
        long total = 0;
        if (!rows.isEmpty()) {
            Object[] r = rows.get(0);          // ▶ 첫 행
            cnt   = ((Number) r[0]).longValue();
            total = ((Number) r[1]).longValue();
        }
        long avg = cnt == 0 ? 0 : total / cnt;

        return Map.of("cnt", cnt, "total", total, "avg", avg);
    }

    /**
     * 최근 활동(간단) – 영수증 도메인 이벤트 기준(제출/승인/반려/마감 등)
     * 성능을 위해 상위 N개만 반환
     */
    @Override
    @Transactional(readOnly = true)
    public List<ReceiptHistoryOverviewDto.Activity> findRecentActivitiesForApprover(
            Integer approverId, String startStr, String endStr, Integer limit) {
        int top = (limit == null || limit <= 0) ? DEFAULT_RECENT_LIMIT : limit;

        // 간단 구현: 내 결재선 기간 조회 결과 중 상위 N개를 시간순으로 변형
        // 실제 운영에서는 별도 이벤트 테이블/로그 테이블 참조 권장
        ReceiptPage<Receipt> page = getMyPendingByDate(approverId, null, startStr, endStr,
                null, 0, top, Sort.by(Sort.Order.desc("submissionDate"), Sort.Order.desc("receiptId")));

        return page.getContent().stream().map(r -> ReceiptHistoryOverviewDto.Activity.builder()
                .timestamp(String.valueOf(r.getSubmissionDate()))
                .label(String.valueOf(r.getStatus()))
                .text(r.getReason())
                .receiptId(r.getReceiptId())
                .receiptCode(r.getReceiptCode())
                .applicantUserId(r.getUserId())
                .applicantUserName(r.getUserName())
                .applicantUserEmail(r.getUserEmail())
                .categoryName(r.getCategory() != null ? r.getCategory().getCategoryName() : null)
                .amount(r.getAmount() != null ? r.getAmount().longValue() : null)
                .build()).toList();
    }

    /**
     * [개요] 개인 화면용 히스토리 개요를 생성한다.
     * - monthlySummary.count  : 내가 제출한 총 건수(기간 내)
     * - monthlySummary.total  : 결재자 관점 합계 금액(기존 로직 유지)
     * - monthlySummary.approvalRate : (APPROVED + CLOSED) / count × 100
     * - myPendingCount        : 내 차례 건수(페이징 1로 총합)
     * - recentActivities      : 상위 N개 활동(간단)
     */
    @Override
    @Transactional(readOnly = true)
    public ReceiptHistoryOverviewDto buildHistoryOverview(Integer approverId, String startDate, String endDate, int limit) {
        // 결재자 관점 합계 금액 등
        Map<String, Long> approverTotals = getTotalsForMyPending(approverId, null, startDate, endDate, null);
        long approverTotalAmount = approverTotals.getOrDefault("total", 0L);

        // 신청자(본인) 관점 승인율 계산
        LocalDate s = LocalDate.parse(startDate);
        LocalDate e = LocalDate.parse(endDate);
        // 정확한 건수 기반 승인율 계산: (APPROVED + CLOSED) / TOTAL
        long submitTotalCnt = receiptSearchRepository.countByUserAndStatuses(
                approverId, Date.valueOf(s), Date.valueOf(e), null);
        long submitApproved = receiptSearchRepository.countByUserAndStatuses(
                approverId, Date.valueOf(s), Date.valueOf(e), List.of(ReceiptProcessStatus.APPROVED));
        long submitClosed   = receiptSearchRepository.countByUserAndStatuses(
                approverId, Date.valueOf(s), Date.valueOf(e), List.of(ReceiptProcessStatus.CLOSED));
        int approvalRate = submitTotalCnt > 0
                ? (int) Math.round(((submitApproved + submitClosed) * 100.0) / submitTotalCnt)
                : 0;

        long myPending = getMyPendingApprovals(approverId, 0, 1).getTotalElements();
        List<ReceiptHistoryOverviewDto.Activity> activities = findRecentActivitiesForApprover(approverId, startDate, endDate, limit);

        return ReceiptHistoryOverviewDto.builder()
                .period(ReceiptHistoryOverviewDto.Period.builder()
                        .startDate(startDate).endDate(endDate).build())
                .monthlySummary(ReceiptHistoryOverviewDto.MonthlySummary.builder()
                        .count(submitTotalCnt)
                        .total(approverTotalAmount)
                        .approvalRate(approvalRate)
                        .build())
                .myPendingCount(myPending)
                .recentActivities(activities)
                .build();
    }
    
    
    @Override
    @Transactional
    public Map<String, Long> getTotalsForMyPendingRoles(
            Integer approverId, Integer searchUserId,
            Integer departmentId, Integer teamId,
            String startStr, String endStr,
            List<Integer> roles) {

        Date start = Date.valueOf(startStr);
        Date end   = Date.valueOf(endStr);
        if (roles == null || roles.isEmpty()) roles = List.of(1); // 기본 = 결재

        Object[] row = receiptRepository
            .findTotalsForApproverByRoles(
                approverId, searchUserId, departmentId, teamId, start, end, roles)
            .get(0);                       // 항상 1행

        long cnt   = ((Number) row[0]).longValue();
        long total = ((Number) row[1]).longValue();
        long avg   = cnt == 0 ? 0 : total / cnt;

        return Map.of("cnt", cnt, "total", total, "avg", avg);
    }

    
    
    @Override
    @Transactional
    public ReceiptPage<ReceiptApprClosedSummaryDto> getApprClosedSummary(
			Integer approverId, Integer userId,
			LocalDate start, LocalDate end,
			List<ReceiptProcessStatus> statuses,
			int page, int size) {
    	
        Pageable pageable = PageRequest.of(page, size, Sort.by("userId"));

        Page<Object[]> raw = receiptRepository.findApprClosedSummary(
		        approverId, userId,
		        Date.valueOf(start), Date.valueOf(end),
		        statuses,
		        pageable);
        
        /* Object[] → DTO */
        List<ReceiptApprClosedSummaryDto> dtoList = raw.getContent().stream()
            .map(r -> ReceiptApprClosedSummaryDto.builder()
                    .userId  ((Integer) r[0])
                    .userName      ((String)  r[1])
                    .userEmail     ((String)  r[2])
                    .departmentId  ((Integer) r[3])
                    .departmentName((String)  r[4])
                    .teamId        ((Integer) r[5])
                    .teamName      ((String)  r[6])
                    .count         (((Number)  r[7]).longValue())
                    .approved      (((Number)  r[8]).longValue())
                    .closed        (((Number)  r[9]).longValue())
                    .build())
            .toList();

        return ReceiptPage.<ReceiptApprClosedSummaryDto>builder()
                .content(dtoList)
                .totalPages(raw.getTotalPages())
                .totalElements(raw.getTotalElements())
                .pageNumber(raw.getNumber())
                .pageSize(raw.getSize())
                .build();
    }

    
    

    /**
     * 선택된 월(YYYY-MM)을 기준으로, 그 이전 월들 중 '승인' 상태의 영수증이 있는 월 목록을 조회합니다.
     * @param currentMonth YYYY-MM 형식의 현재 조회 월
     * @return YYYY-MM 형식의 과거 승인 월 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<String> findPastApprovedMonths(String currentMonth) {
        // currentMonth의 첫째 날을 기준으로 그 이전 날짜를 조회
        LocalDate firstDayOfCurrentMonth = LocalDate.parse(currentMonth + "-01");
        Date givenDate = Date.valueOf(firstDayOfCurrentMonth);

        return receiptRepository.findMonthsWithStatusBeforeDate(
            givenDate,
            ReceiptProcessStatus.APPROVED
        );
    }
    
    
    
    /**
     * 사용자 비활성화 및 권한 변경으로 인한 영수증 결재선 목록 조회 (페이징)
     * @param approverId 결재자 ID
     * @param page       페이지 번호
     * @param size       페이지 크기
     * @return 페이징된 영수증 목록 DTO
     */
    @Override
    @Transactional(readOnly = true)
    public ReceiptPage<Receipt> getMyPendingApprovals(Integer approverId, int page, int size) {
        // 정렬 기준: 제출일 최신순, ID 최신순
        Sort sort = Sort.by(
            Sort.Order.desc("submissionDate"),
            Sort.Order.desc("receiptId")
        );
        Pageable pageable = PageRequest.of(page, size, sort);

        // 조회할 상태를 리스트로 정의
//        List<ReceiptProcessStatus> statusesToSearch = List.of(
//        	ReceiptProcessStatus.WAITING,
//            ReceiptProcessStatus.REQUEST,
//            ReceiptProcessStatus.REJECTED
//        );

        // Repository 메소드에 상태 리스트를 전달
//        Page<Receipt> receiptPage = receiptRepository.findMyPendingApprovals(
//            approverId,
//            statusesToSearch,
//            pageable
//        );
        Page<Receipt> receiptPage = receiptRepository.findMyPendingApprovals(
                approverId,
                pageable
            );

        return ReceiptConverter.toReceiptPage(receiptPage);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPendingCountsByRole(Integer approverId) {
        List<Object[]> rows = receiptRepository.findPendingCountsByRole(approverId);
        long approvals = 0;
        long concurrences = 0;
        if (rows != null && !rows.isEmpty()) {
            Object[] r = rows.get(0);
            approvals    = r[0] != null ? ((Number) r[0]).longValue() : 0L;
            concurrences = r[1] != null ? ((Number) r[1]).longValue() : 0L;
        }

        /* 월 목록 조회 */
        List<String> approvalMonths = receiptRepository.findPendingMonthsByRoles(approverId, List.of(1));
        List<String> concurrenceMonths = receiptRepository.findPendingMonthsByRoles(approverId, List.of(2,3));

        return Map.of(
            "approvals", approvals,
            "concurrences", concurrences,
            "approvalMonths", approvalMonths,
            "concurrenceMonths", concurrenceMonths
        );
    }
}

