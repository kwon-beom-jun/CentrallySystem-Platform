package com.cs.rcpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cs.rcpt.entity.ReceiptDefaultApprover;

import java.util.Optional;

public interface ReceiptDefaultApproverRepository extends JpaRepository<ReceiptDefaultApprover, Long> {

	Optional<ReceiptDefaultApprover> findByEmail(String email);

	boolean existsByEmail(String email);
	
	/** 가장 큰 step 번호 (없으면 0) */
    @Query("select coalesce(max(r.stepNo),0) from ReceiptDefaultApprover r")
    int findMaxStepNo(); 
}
