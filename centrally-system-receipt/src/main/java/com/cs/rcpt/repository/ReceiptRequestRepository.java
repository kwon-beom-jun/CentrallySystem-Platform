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
 * (조회/검색용)
 * ReceiptRequestService가 사용하는 리포지토리
 * 결재/반려 처리 등 실제 데이터 상태를 변경하는 '액션'에 필요한 조회 쿼리를 담당
 * (예: 일괄 처리를 위해 대상 영수증을 찾는 쿼리)
 */
public interface ReceiptRequestRepository {
    
    
    @Query("""
	   SELECT DISTINCT r
	     FROM Receipt r
	     JOIN r.approvalLines l
	    WHERE l.approverUserId = :approverId
	      AND l.approvalStatus = false
	      AND l.stepOrder      = r.currentApprovalStep
	      AND r.status         = com.cs.rcpt.enums.ReceiptProcessStatus.REQUEST
	      AND l.approvalRole   IN :roles 
	      AND r.submissionDate BETWEEN :start AND :end
	      AND r.userId         IN :userIds
	""")
	List<Receipt> findMyTurnReceipts(
	    @Param("approverId") Integer approverId,
	    @Param("userIds")    List<Integer> userIds,
	    @Param("start")      Date start,
	    @Param("end")        Date end,
	    @Param("roles")      List<Integer> roles);

}
