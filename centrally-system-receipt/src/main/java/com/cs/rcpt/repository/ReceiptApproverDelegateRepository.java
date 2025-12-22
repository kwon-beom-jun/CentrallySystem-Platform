package com.cs.rcpt.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import com.cs.rcpt.entity.ReceiptApproverDelegate;

public interface ReceiptApproverDelegateRepository extends JpaRepository<ReceiptApproverDelegate, Long> {

    @Query("""
        SELECT d FROM ReceiptApproverDelegate d
        WHERE d.principalUserId = :principal
          AND (d.effectiveFrom IS NULL OR d.effectiveFrom <= :now)
          AND (d.effectiveTo   IS NULL OR d.effectiveTo   >= :now)
          AND d.enabled = true
    """)
    Optional<ReceiptApproverDelegate> findActiveDelegate(
        @Param("principal") Integer principalUserId,
        @Param("now") LocalDateTime now
    );

    @Query("""
        SELECT d FROM ReceiptApproverDelegate d
        WHERE d.delegateUserId = :delegate
          AND (d.effectiveFrom IS NULL OR d.effectiveFrom <= :now)
          AND (d.effectiveTo   IS NULL OR d.effectiveTo   >= :now)
          AND d.enabled = true
    """)
    java.util.List<ReceiptApproverDelegate> findActivePrincipalsByDelegate(
        @Param("delegate") Integer delegateUserId,
        @Param("now") LocalDateTime now
    );

    @Modifying
    @Query("""
        UPDATE ReceiptApproverDelegate d
           SET d.enabled = false,
               d.deletedAt = CURRENT_TIMESTAMP
         WHERE d.enabled = true
           AND (d.principalUserId = :userId OR d.delegateUserId = :userId)
    """)
    int softDeleteAllByUserId(@Param("userId") Integer userId);
}



