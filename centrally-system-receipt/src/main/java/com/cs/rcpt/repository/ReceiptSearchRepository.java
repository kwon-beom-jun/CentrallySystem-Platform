package com.cs.rcpt.repository;

import com.cs.rcpt.entity.Receipt;
import com.cs.rcpt.enums.ReceiptProcessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

/**
 * (액션용)
 * ReceiptSearchService가 사용하는 리포지토리
 * 결재자/관리자 관점의 복잡한 '검색, 목록, 통계' 조회 쿼리를 모두 담당
 */
public interface ReceiptSearchRepository {
	
	/** 
	 *  결재선[내가(approverId)]에 포함된 영수증만 월/일 집계 
        
        신청자
        r.userId,
        총 건수
        COUNT(r),

        대기(접수前)
        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.WAITING
                 THEN r.amount ELSE 0 END),

        신청 진행 중
        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.REQUEST
                 THEN r.amount ELSE 0 END),

        승인 완료
        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.APPROVED
                 THEN r.amount ELSE 0 END),

        반려
        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.REJECTED
                 THEN r.amount ELSE 0 END),

        마감(지급까지 완료)
        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.CLOSED
                 THEN r.amount ELSE 0 END)
        
		WHERE 	l.approverUserId = :approverId           	 -- 내가 결재자인 라인만
				AND r.submissionDate BETWEEN :start AND :end -- 기간
				AND (:userId IS NULL OR r.userId = :userId)  -- 신청자(선택)
				r.userId,                                  	 -- 신청자
				COUNT(r),                                  	 -- 건수
	 */
	@Query("""
    SELECT
        r.userId,
        r.userName,
        r.userEmail,
        r.departmentId,
        r.departmentName,
        r.teamId,
        r.teamName,
        COUNT(r),
        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.WAITING
                 THEN r.amount ELSE 0 END),
        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.REQUEST
                 THEN r.amount ELSE 0 END),
        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.APPROVED
                 THEN r.amount ELSE 0 END),
        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.REJECTED
                 THEN r.amount ELSE 0 END),
        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.CLOSED
                 THEN r.amount ELSE 0 END)
	    FROM   Receipt r JOIN r.approvalLines l
	    WHERE  l.approverUserId      = :approverId
	      AND  r.submissionDate BETWEEN :start AND :end
	      AND (:userId    IS NULL  OR r.userId  = :userId)
	      AND (:statuses  IS NULL  OR r.status IN :statuses)
	    GROUP  BY
	        r.userId, r.userName, r.userEmail,
	        r.departmentId, r.departmentName,
	        r.teamId, r.teamName
	""")
	Page<Object[]> findSummaryForMyApproval(
	        @Param("approverId") Integer approverId,
	        @Param("userId")     Integer userId,   // (=검색창에서 선택한 ‘이름’)
	        @Param("start")      Date    start,
	        @Param("end")        Date    end,
	        @Param("statuses")   List<ReceiptProcessStatus> statuses,
	        Pageable pageable);

	
	/**
	 * 기간·필터별 전체 합계 + 상태별 금액 합계를 1 row 로 반환
	 *
	 * 0 : 총 건수
	 * 1 : WAITING  금액
	 * 2 : REQUEST  금액
	 * 3 : APPROVED 금액
	 * 4 : REJECTED 금액
	 * 5 : CLOSED   금액
	 */
	@Query("""
	    SELECT
	        COUNT(r),
	        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.WAITING   THEN r.amount ELSE 0 END),
	        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.REQUEST   THEN r.amount ELSE 0 END),
	        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.APPROVED  THEN r.amount ELSE 0 END),
	        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.REJECTED  THEN r.amount ELSE 0 END),
	        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.CLOSED    THEN r.amount ELSE 0 END)
	    FROM   Receipt r
	    /* ────────────── approverId 가 있을 때만 존재 여부 검사 ────────────── */
	    WHERE  r.submissionDate BETWEEN :start AND :end
	      AND (:userId      IS NULL OR  r.userId = :userId)
	      AND (:approverId  IS NULL OR
	           EXISTS (SELECT 1 FROM r.approvalLines al
	                   WHERE al.approverUserId = :approverId))
	      AND (:statuses    IS NULL OR  r.status IN :statuses)
	""")
	List<Object[]> findTotalsDynamic(      // 반환은 항상 1 row
	        @Param("start")      Date start,
	        @Param("end")        Date end,
	        @Param("approverId") Integer approverId,         // NULL 허용
	        @Param("userId")     Integer userId,             // NULL 허용
	        @Param("statuses")   List<ReceiptProcessStatus> statuses);  // NULL 또는 0 ~ N


	/**
	 * 신청자(userId) 기준, 기간 내 특정 상태 집계(건수 전용)
	 */
	@Query("""
			SELECT COUNT(r)
				FROM Receipt r
				WHERE r.userId = :userId
					AND r.submissionDate BETWEEN :start AND :end
					AND (:statuses IS NULL OR r.status IN :statuses)
	""")
	long countByUserAndStatuses(
					@Param("userId") Integer userId,
					@Param("start")  Date start,
					@Param("end")    Date end,
					@Param("statuses") List<ReceiptProcessStatus> statuses);
	
	
	/** ==============================================================
	 * ② **전체/특정 상태** : APPROVED · REJECTED · 요청 無(전체) 모두 처리
//	WHERE l.approverUserId = :approverId          -- 내가 결재선에 포함
//			AND r.submissionDate BETWEEN :start AND :end
//			AND (:userId IS NULL  OR r.userId = :userId) -- 특정 신청자
//			AND (:status IS NULL  OR r.status = :status) -- status 없으면 전체
	 * --------------------------------------------------------------*/
	 /* ② 전체 또는 다중 상태(IN) */
	@Query("""
       SELECT DISTINCT r
         FROM Receipt r JOIN r.approvalLines l
        WHERE l.approverUserId = :approverId
          AND r.submissionDate BETWEEN :start AND :end
          AND (:userId   IS NULL OR r.userId = :userId)
          AND (:statuses IS NULL OR r.status IN :statuses)
	""")
	Page<Receipt> findByApproverAndDateRange(
	        @Param("approverId") Integer approverId,
	        @Param("userId")     Integer userId,
	        @Param("start")      Date    start,
	        @Param("end")        Date    end,
	        @Param("statuses")   List<ReceiptProcessStatus> statuses,
	        Pageable pageable);

	
	
	/** ==============================================================
	 * ① **내 차례(PENDING 전용)** : status = REQUEST 인 경우만 사용
	 * --------------------------------------------------------------*/
	@Query("""
	       SELECT r
	         FROM Receipt r JOIN r.approvalLines l
	        WHERE l.approverUserId = :approverId
	          AND l.approvalStatus = false
	          AND l.stepOrder      = r.currentApprovalStep
	          AND r.status         = com.cs.rcpt.enums.ReceiptProcessStatus.REQUEST
	          AND r.submissionDate BETWEEN :start AND :end
	          AND (:userId IS NULL OR r.userId = :userId)
	""")
	Page<Receipt> findPendingApprovalsOnly(
	        @Param("approverId") Integer approverId,
	        @Param("userId")     Integer userId,
	        @Param("start")      Date    start,
	        @Param("end")        Date    end,
	        Pageable pageable);
	
	
	/**
	 * ‘내 차례’ + 결재(1) / 합의(2 or 3) 라인만 조회
	 */
	@Query("""
       SELECT r
         FROM Receipt r JOIN r.approvalLines l
        WHERE l.approverUserId = :approverId
          AND l.approvalStatus = false
          AND l.stepOrder      = r.currentApprovalStep
          AND r.status         = com.cs.rcpt.enums.ReceiptProcessStatus.REQUEST
          AND (:roles IS NULL OR l.approvalRole IN :roles)
          AND r.submissionDate BETWEEN :start AND :end
          AND (:userId IS NULL OR r.userId = :userId)
   """)
	Page<Receipt> findPendingApprovalsByRoles(
	        @Param("approverId") Integer approverId,
	        @Param("userId")     Integer userId,
	        @Param("start")      Date    start,
	        @Param("end")        Date    end,
	        @Param("roles")      List<Integer> roles,
	        Pageable pageable);

	/**
	 * 위임(대리결재자) 관점의 ‘내 차례’ + 역할 필터 조회
	 */
	@Query("""
       SELECT r
         FROM Receipt r JOIN r.approvalLines l
        WHERE l.approvalStatus = false
          AND l.stepOrder      = r.currentApprovalStep
          AND r.status         = com.cs.rcpt.enums.ReceiptProcessStatus.REQUEST
          AND (:roles IS NULL OR l.approvalRole IN :roles)
          AND r.submissionDate BETWEEN :start AND :end
          AND (:userId IS NULL OR r.userId = :userId)
          AND EXISTS (
                SELECT 1 FROM ReceiptApproverDelegate d
                 WHERE d.principalUserId = l.approverUserId
                   AND d.delegateUserId  = :delegateId
                   AND d.enabled = true
                   AND (d.effectiveFrom IS NULL OR d.effectiveFrom <= :now)
                   AND (d.effectiveTo   IS NULL OR d.effectiveTo   >= :now)
          )
    """)
	Page<Receipt> findPendingApprovalsByDelegateRoles(
	        @Param("delegateId") Integer delegateId,
	        @Param("userId")     Integer userId,
	        @Param("start")      Date    start,
	        @Param("end")        Date    end,
	        @Param("roles")      List<Integer> roles,
	        @Param("now")        java.time.LocalDateTime now,
	        Pageable pageable);

	/**
	 * 위임(대리결재자) 관점 신청자별 집계
	 */
	@Query("""
	   SELECT
	       r.userId           ,
	       r.userName         , r.userEmail       ,
	       r.departmentId     , r.departmentName  ,
	       r.teamId           , r.teamName        ,
	       COUNT(r)           ,
	       SUM(r.amount)
	   FROM   Receipt r JOIN r.approvalLines l
	   WHERE  l.approvalStatus = false
	     AND  l.stepOrder      = r.currentApprovalStep
	     AND  r.status         = com.cs.rcpt.enums.ReceiptProcessStatus.REQUEST
	     AND  r.submissionDate BETWEEN :start AND :end
	     AND (:roles IS NULL OR l.approvalRole IN :roles)
	     AND (:userId IS NULL OR r.userId = :userId)
	     AND EXISTS (
	           SELECT 1 FROM ReceiptApproverDelegate d
	            WHERE d.principalUserId = l.approverUserId
	              AND d.delegateUserId  = :delegateId
	              AND d.enabled = true
	              AND (d.effectiveFrom IS NULL OR d.effectiveFrom <= :now)
	              AND (d.effectiveTo   IS NULL OR d.effectiveTo   >= :now)
	     )
	   GROUP BY
	       r.userId, r.userName, r.userEmail,
	       r.departmentId, r.departmentName,
	       r.teamId, r.teamName
	""")
	Page<Object[]> findPendingSummaryByUserAsDelegate(
	        @Param("delegateId")   Integer delegateId,
	        @Param("userId")       Integer userId,
	        @Param("start")        Date    start,
	        @Param("end")          Date    end,
	        @Param("roles")        List<Integer> roles,
	        @Param("now")          java.time.LocalDateTime now,
	        Pageable pageable);

	
	/**
	 * ‘내 차례(REQUEST)’ + 결재/합의 역할 필터 후
	 *  신청자별 건수 · 금액 합계를 1 row 로 집계
	 *
	 * 0 userId · 1 userName · 2 userEmail · 3 departmentId · 4 departmentName
	 * 5 teamId · 6 teamName · 7 건수 · 8 총금액
	 */
	@Query("""
	   SELECT
	       r.userId           ,
	       r.userName         , r.userEmail       ,
	       r.departmentId     , r.departmentName  ,
	       r.teamId           , r.teamName        ,
	       COUNT(r)           ,
	       SUM(r.amount)
	   FROM   Receipt r JOIN r.approvalLines l
	   WHERE  l.approverUserId = :approverId
	     AND  l.approvalStatus = false
	     AND  l.stepOrder      = r.currentApprovalStep
	     AND  r.status         = com.cs.rcpt.enums.ReceiptProcessStatus.REQUEST
	     AND  r.submissionDate BETWEEN :start AND :end
	     AND (:roles IS NULL OR l.approvalRole IN :roles)
	     AND (:userId IS NULL OR r.userId = :userId)
	     AND (:teamId IS NULL OR r.teamId = :teamId)
	     AND (:teamId IS NOT NULL OR :departmentId IS NULL OR r.departmentId = :departmentId)
	   GROUP BY
	       r.userId, r.userName, r.userEmail,
	       r.departmentId, r.departmentName,
	       r.teamId, r.teamName
	""")
	Page<Object[]> findPendingSummaryByUser(
	        @Param("approverId")   Integer approverId,
	        @Param("userId")       Integer userId,
	        @Param("departmentId") Integer departmentId,
	        @Param("teamId")       Integer teamId,
	        @Param("start")        Date    start,
	        @Param("end")          Date    end,
	        @Param("roles")        List<Integer> roles,
	        Pageable pageable);
	
	/**
	 * 대리결재자(delegateId) 관점에서, 기간 내 (상태/신청자 선택) 영수증 전체 조회
	 * - pending 전용 조건(l.approvalStatus=false, r.status=REQUEST)을 제거하고
	 *   ReceiptApproverDelegate 매핑 존재 여부만 검사
	 */
	@Query("""
	       SELECT DISTINCT r
	         FROM Receipt r JOIN r.approvalLines l
	        WHERE r.submissionDate BETWEEN :start AND :end
	          AND (:userId   IS NULL OR r.userId = :userId)
	          AND (:statuses IS NULL OR r.status IN :statuses)
	          AND EXISTS (
	                SELECT 1 FROM ReceiptApproverDelegate d
	                 WHERE d.principalUserId = l.approverUserId
	                   AND d.delegateUserId  = :delegateId
	                   AND d.enabled = true
	                   AND (d.effectiveFrom IS NULL OR d.effectiveFrom <= :now)
	                   AND (d.effectiveTo   IS NULL OR d.effectiveTo   >= :now)
	          )
	""")
	Page<Receipt> findByDelegateAndDateRange(
	        @Param("delegateId") Integer delegateId,
	        @Param("userId")     Integer userId,
	        @Param("start")      Date    start,
	        @Param("end")        Date    end,
	        @Param("statuses")   List<ReceiptProcessStatus> statuses,
	        @Param("now")        java.time.LocalDateTime now,
	        Pageable pageable);

	/**
	 * 대리결재자(delegateId) 관점 신청자별 집계 (전체 상태)
	 * - approver 요약(findSummaryForMyApproval)의 delegate 버전
	 */
	@Query("""
	    SELECT
	        r.userId,
	        r.userName,
	        r.userEmail,
	        r.departmentId,
	        r.departmentName,
	        r.teamId,
	        r.teamName,
	        COUNT(r),
	        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.WAITING  THEN r.amount ELSE 0 END),
	        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.REQUEST  THEN r.amount ELSE 0 END),
	        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.APPROVED THEN r.amount ELSE 0 END),
	        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.REJECTED THEN r.amount ELSE 0 END),
	        SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.CLOSED   THEN r.amount ELSE 0 END)
	    FROM   Receipt r JOIN r.approvalLines l
	    WHERE  r.submissionDate BETWEEN :start AND :end
	      AND (:userId    IS NULL  OR r.userId  = :userId)
	      AND (:statuses  IS NULL  OR r.status IN :statuses)
	      AND EXISTS (
	            SELECT 1 FROM ReceiptApproverDelegate d
	             WHERE d.principalUserId = l.approverUserId
	               AND d.delegateUserId  = :delegateId
	               AND d.enabled = true
	               AND (d.effectiveFrom IS NULL OR d.effectiveFrom <= :now)
	               AND (d.effectiveTo   IS NULL OR d.effectiveTo   >= :now)
	      )
	    GROUP BY
	        r.userId, r.userName, r.userEmail,
	        r.departmentId, r.departmentName,
	        r.teamId, r.teamName
	""")
	Page<Object[]> findSummaryForDelegateApproval(
	        @Param("delegateId") Integer delegateId,
	        @Param("userId")     Integer userId,
	        @Param("start")      Date    start,
	        @Param("end")        Date    end,
	        @Param("statuses")   List<ReceiptProcessStatus> statuses,
	        @Param("now")        java.time.LocalDateTime now,
	        Pageable pageable);
	
	
	
	/**
	 * 영수증 신청 내역 조회 통계
	 * ‘내 차례’(PENDING) 건수·금액만 집계
	 */
	@Query("""
       SELECT COUNT(DISTINCT r.receiptId),
              COALESCE(SUM(r.amount), 0)
         FROM Receipt r
         JOIN r.approvalLines l
        WHERE l.approverUserId = :approverId
          AND r.submissionDate BETWEEN :start AND :end
          AND (:searchUserId IS NULL OR r.userId = :searchUserId)

          /* ─── 상태 필터 ─────────────────────────────────────────────── */
          AND (
               /* ① 신청(REQUEST)만 → 내 차례 + 미승인 + r.status = REQUEST */
               (:status = com.cs.rcpt.enums.ReceiptProcessStatus.REQUEST
                    AND l.stepOrder      = r.currentApprovalStep
                    AND l.approvalStatus = false
                    AND r.status         = com.cs.rcpt.enums.ReceiptProcessStatus.REQUEST)

               /* ② APPROVED / REJECTED / 기타 특정 상태만 */
            OR (:status IS NOT NULL
                    AND :status <> com.cs.rcpt.enums.ReceiptProcessStatus.REQUEST
                    AND r.status = :status)

               /* ③ 전체(null) */
            OR (:status IS NULL)
          )
	""")
	List<Object[]> findTotalsDynamicForApprover(
	        @Param("approverId") Integer approverId,
	        @Param("searchUserId")     Integer searchUserId,
	        @Param("start")      Date    start,
	        @Param("end")        Date    end,
	        @Param("status")     ReceiptProcessStatus status);

	
	
	/**
	      결재자 필터가 있을 때만 EXISTS 로 체크 (중복 無)
	      AND  (:approverId IS NULL OR
	            EXISTS (SELECT 1 FROM r.approvalLines al
	                    WHERE al.approverUserId = :approverId))
	      ④ 승인·마감 상태만
	      AND  r.status IN (com.cs.rcpt.enums.ReceiptProcessStatus.APPROVED,
	                        com.cs.rcpt.enums.ReceiptProcessStatus.CLOSED)
	      ⑤ (선택) 특정 신청자
	      AND  (:userId IS NULL OR r.userId = :userId) 
	 */
//    AND  r.status IN (com.cs.rcpt.enums.ReceiptProcessStatus.APPROVED,
//            		  com.cs.rcpt.enums.ReceiptProcessStatus.CLOSED)
	@Query("""
		    SELECT
		        r.userId,
		        r.userName,
		        r.userEmail,
		        r.departmentId,
		        r.departmentName,
		        r.teamId,
		        r.teamName,
		        COUNT(DISTINCT r),
		        SUM(DISTINCT CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.APPROVED
		                          THEN r.amount ELSE 0 END),
		        SUM(DISTINCT CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.CLOSED
		                          THEN r.amount ELSE 0 END)
		    FROM   Receipt r
		    WHERE  r.submissionDate BETWEEN :start AND :end
		      AND  (:approverId IS NULL OR
		            EXISTS (SELECT 1 FROM r.approvalLines al
		                    WHERE al.approverUserId = :approverId))
		      AND  r.status IN :statuses
		      AND  (:userId IS NULL OR r.userId = :userId)
		    GROUP BY
		        r.userId, r.userName, r.userEmail,
		        r.departmentId, r.departmentName,
		        r.teamId, r.teamName
		""")
	Page<Object[]> findApprClosedSummary(
	        @Param("approverId") Integer approverId,
	        @Param("userId")     Integer userId,
	        @Param("start")      Date    start,
	        @Param("end")        Date    end,
	        @Param("statuses")   List<ReceiptProcessStatus> statuses,
	        Pageable pageable);

		
		
	/**
	 * ‘내 차례’ + 역할(approvalRole) 조건까지 포함한 건수·금액 통계
	 *  0: 건수, 1: 금액
	 */
	@Query("""
	   SELECT
	       COUNT(DISTINCT r.receiptId),
	       COALESCE(SUM(r.amount), 0)
	     FROM Receipt r JOIN r.approvalLines l
	    WHERE l.approverUserId = :approverId
	      AND l.approvalStatus = false
	      AND l.stepOrder      = r.currentApprovalStep
	      AND r.status         = com.cs.rcpt.enums.ReceiptProcessStatus.REQUEST
	      AND (:roles IS NULL OR l.approvalRole IN :roles)
	      AND r.submissionDate BETWEEN :start AND :end
	      AND (:searchUserId IS NULL OR r.userId = :searchUserId)
	      AND (:teamId IS NULL OR r.teamId = :teamId)
	      AND (:teamId IS NOT NULL OR :departmentId IS NULL OR r.departmentId = :departmentId)
	""")
	List<Object[]> findTotalsForApproverByRoles(
	        @Param("approverId")  Integer approverId,
	        @Param("searchUserId")Integer searchUserId,
	        @Param("departmentId") Integer departmentId,
	        @Param("teamId")       Integer teamId,
	        @Param("start")       Date    start,
	        @Param("end")         Date    end,
	        @Param("roles")       List<Integer> roles);

	
	
	
    /**
     * 특정 날짜 이전, 'APPROVED' 상태의 영수증이 존재하는 월(YYYY-MM) 목록을 조회합니다.
     * @param givenDate 기준 날짜 (이 날짜 이전의 월을 조회)
     * @param status 조회할 영수증 상태
     * @return YYYY-MM 형식의 월 목록
     */
    @Query("""
        SELECT DISTINCT CONCAT(YEAR(r.submissionDate), '-', LPAD(CAST(MONTH(r.submissionDate) AS string), 2, '0'))
        FROM Receipt r
        WHERE r.submissionDate < :givenDate
          AND r.status = :status
        ORDER BY 1 DESC
    """)
    List<String> findMonthsWithStatusBeforeDate(
        @Param("givenDate") Date givenDate,
        @Param("status") ReceiptProcessStatus status 
    );
    

    /**
     * 특정 결재자가 포함된 영수증 조회 (사용자 비활성화 처리용)
     * - REQUEST 상태: 해당 결재자가 '미승인' 상태인 건만 조회
     * - (제외) WAITING, REJECTED 상태: 해당 결재자가 포함된 모든 건 조회
     *
     * @param approverId 결재자 ID
     * @param pageable   페이징 및 정렬 정보
     * @return 페이징된 영수증 목록
     */
//    @Query("""
//        SELECT DISTINCT r FROM Receipt r JOIN r.approvalLines al
//        WHERE al.approverUserId = :approverId
//          AND (
//            (r.status = com.cs.rcpt.enums.ReceiptProcessStatus.REQUEST AND al.approvalStatus = false)
//            OR
//            (r.status IN (com.cs.rcpt.enums.ReceiptProcessStatus.WAITING, com.cs.rcpt.enums.ReceiptProcessStatus.REJECTED))
//          )
//    """)
    @Query("""
        SELECT DISTINCT r FROM Receipt r JOIN r.approvalLines al
        WHERE al.approverUserId = :approverId
          AND r.status = com.cs.rcpt.enums.ReceiptProcessStatus.REQUEST AND al.approvalStatus = false
    """)
    Page<Receipt> findMyPendingApprovals(
            @Param("approverId") Integer approverId,
            Pageable pageable);
	
	
	/**
     * '내 차례(REQUEST)' 상태에서, 지정된 approvalRole 목록에 해당하는 영수증이 존재하는 월(YYYY-MM) 목록 조회
     *   - submissionDate 기준, DISTINCT YYYY-MM 문자열 반환
     */
    @Query("""
        SELECT DISTINCT CONCAT(YEAR(r.submissionDate), '-', LPAD(CAST(MONTH(r.submissionDate) AS string), 2, '0'))
          FROM Receipt r JOIN r.approvalLines l
         WHERE l.approverUserId = :approverId
           AND l.approvalStatus = false
           AND l.stepOrder      = r.currentApprovalStep
           AND r.status         = com.cs.rcpt.enums.ReceiptProcessStatus.REQUEST
           AND (:roles IS NULL OR l.approvalRole IN :roles)
         ORDER BY 1 DESC
    """)
    List<String> findPendingMonthsByRoles(@Param("approverId") Integer approverId,
                                          @Param("roles") List<Integer> roles);
	
	
	/**
     * ‘내 차례’ 결재·합의 건수를 합산하여 반환합니다.
     *  0: approvals(결재) 건수, 1: concurrences(합의) 건수
     */
    @Query("""
        SELECT
            SUM(CASE WHEN l.approvalRole = 1 THEN 1 ELSE 0 END),
            SUM(CASE WHEN l.approvalRole IN (2,3) THEN 1 ELSE 0 END)
        FROM Receipt r JOIN r.approvalLines l
        WHERE l.approverUserId = :approverId
          AND l.approvalStatus = false
          AND l.stepOrder      = r.currentApprovalStep
          AND r.status         = com.cs.rcpt.enums.ReceiptProcessStatus.REQUEST
    """)
    List<Object[]> findPendingCountsByRole(@Param("approverId") Integer approverId);
	
	
}
