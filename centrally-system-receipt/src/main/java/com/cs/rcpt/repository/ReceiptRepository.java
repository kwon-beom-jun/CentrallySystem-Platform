package com.cs.rcpt.repository;

import java.sql.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.cs.rcpt.entity.Receipt;
import com.cs.rcpt.enums.ReceiptProcessStatus;

/**
	구분		JPQL (@Query)							Native SQL (@Query(value=..., nativeQuery=true))
	문법		엔티티 기준 문법							실제 DB 쿼리
	사용 		방법	@Query("...") or @Query("""...""")	@Query(value = "...", nativeQuery = true)
	특징		Spring이 SQL 생성해서 실행					직접 SQL 작성
	주석 		가능	❌ 불가 (-- 사용 시 오류 발생)			✅ 가능 (DB가 허용하면)
	복잡한 	쿼리	복잡한 건 제약 있음						자유롭게 작성 가능
 */
/**
 * JpaRepository를 상속받는 메인 리포지토리
 * 주로 ReceiptService가 사용하는 신청자 중심의 기본 조회 및 생성/수정/삭제(CRUD) 기능을 담당
 */
@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Integer>,
											ReceiptRequestRepository,
											ReceiptSearchRepository,
											ReceiptAdminRepository {

    // 사용자(userId)로 영수증 조회
    Page<Receipt> findByUserId(Integer userId, Pageable pageable);
    
    // 전체(모든 사용자) 날짜 범위 검색을 위한 메서드
    Page<Receipt> findBySubmissionDateBetween(
            java.sql.Date startDate,
            java.sql.Date endDate,
            Pageable pageable
    );
    
    // 사용자 + 상태코드(ReceiptStatus.statusCode) 로 페이징 조회
    // userId AND status_code
    Page<Receipt> findByUserIdAndStatus(
            Integer userId,
            ReceiptProcessStatus status,
            Pageable pageable
    );
    
    // 사용자 + 다중 상태코드(ReceiptStatus.statusCode) 로 페이징 조회
    Page<Receipt> findByUserIdAndStatusIn(Integer userId,
    		List<ReceiptProcessStatus> statuses,
            Pageable pageable);
    
    // 사용자(userId) 및 년월로 영수증 조회
    @Query("SELECT r FROM Receipt r " +
	       "WHERE r.userId = :userId " +
	       "  AND r.submissionDate BETWEEN :startDate AND :endDate")
	Page<Receipt> findByUserIdAndSubmissionDateBetween(
	        @Param("userId") Integer userId,
	        @Param("startDate") java.sql.Date startDate,
	        @Param("endDate") java.sql.Date endDate,
	        Pageable pageable
	);
	
    // 사용자(userId) 및 년월일, 상태로 조회
    // userId, 시작/종료일, 상태값을 파라미터로 받아, 상태값이 null이면 전체 조회, 있으면 해당 상태만 필터링
    @Query("SELECT r FROM Receipt r " +
           "WHERE (:userId IS NULL OR r.userId = :userId) " +
           "  AND r.submissionDate BETWEEN :startDate AND :endDate " +
           "  AND (:status IS NULL OR r.status = :status)")
    Page<Receipt> findReceiptDynamic(
            @Param("userId") Integer userId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            @Param("status") ReceiptProcessStatus status,
            Pageable pageable
    );
    
    
    @Query(value = 
	  "SELECT EXTRACT(MONTH FROM r.submission_date) as monthValue, r.status_code, SUM(r.amount) as sumAmount " +
	  "FROM receipt r " +
	  "WHERE r.user_id = :userId " +
	  "  AND EXTRACT(YEAR FROM r.submission_date) = :year " +
	  "GROUP BY EXTRACT(MONTH FROM r.submission_date), r.status_code " +
	  "ORDER BY monthValue, r.status_code",
	  nativeQuery = true)
	List<Object[]> findYearlySummaryByUserId(
	   @Param("userId") Integer userId,
	   @Param("year") int year
	);
	
	
	@Query("""
	    SELECT 
			r.userId,
			COUNT(r),
			SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.REQUEST  THEN r.amount ELSE 0 END),
			SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.APPROVED THEN r.amount ELSE 0 END),
			SUM(CASE WHEN r.status = com.cs.rcpt.enums.ReceiptProcessStatus.REJECTED  THEN r.amount ELSE 0 END)
	    FROM Receipt r
	    WHERE (:userId IS NULL OR r.userId = :userId)
	      AND r.submissionDate BETWEEN :startDate AND :endDate
	    GROUP BY r.userId
	""")
	List<Object[]> findSummaryByUserAndDateRange(
	        @Param("userId") Integer userId,
	        @Param("startDate") Date startDate,
	        @Param("endDate") Date endDate
	);


	
	
	@Query("""
       SELECT r FROM Receipt r
       WHERE r.userId = :userId
         AND r.submissionDate BETWEEN :start AND :end
         AND (:statuses IS NULL OR r.status IN :statuses)
	""")
	Page<Receipt> findByUserAndDateRange(
	        @Param("userId")   Integer userId,
	        @Param("start")    Date start,
	        @Param("end")      Date end,
	        @Param("statuses") List<ReceiptProcessStatus> statuses,
	        Pageable pageable);	

    
    /**
     * (미사용)특정 결재자가 들어간 결재선 조회
     * - 영수증 상태가 대기, 신청, 반려
     *
     * @param approverId 결재자(본인) ID
     * @param statuses   영수증의 현재 처리 상태
     * @param pageable   페이징 및 정렬 정보
     * @return 페이징된 영수증 목록
     */
    @Query("SELECT r FROM Receipt r JOIN r.approvalLines al " +
           "WHERE al.approverUserId = :approverId " +
           "  AND r.status IN :statuses ")
    Page<Receipt> findMyPendingApprovals(@Param("approverId") Integer approverId,
    									 @Param("statuses") List<ReceiptProcessStatus> statuses,
                                         Pageable pageable);
    
	
}
