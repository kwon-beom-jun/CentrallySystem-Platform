package com.cs.rcpt.repository;

import com.cs.rcpt.entity.ReceiptApprovalLine;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptApprovalLineRepository extends JpaRepository<ReceiptApprovalLine, Integer> {

    /* 반려 결재선 → ‘대기’ 상태로 초기화 */
	// learAutomatically = true 때문에 JPQL‐update 를 실행한 직후에 1차 캐시(영속성 컨텍스트)가 싹 비워짐
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE ReceiptApprovalLine al
           SET al.approvedAt      = NULL,
               al.rejectedAt      = NULL,
               al.rejectedReason  = NULL,
               al.approvalStatus  = FALSE,
               al.delegateMapping = NULL
         WHERE al.receipt.receiptId = :receiptId
    """)
    int resetByReceiptId(@Param("receiptId") Integer receiptId);
}
