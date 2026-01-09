package com.cs.rcpt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.cs.rcpt.entity.ReceiptApproverFavorite;

public interface ReceiptApproverFavoriteRepository extends JpaRepository<ReceiptApproverFavorite, Long> {

	List<ReceiptApproverFavorite> findByOwnerUserIdOrderByStepNoAsc(Long ownerUserId);

	boolean existsByOwnerUserIdAndFavoriteUserId(Long ownerUserId, Long favoriteUserId);

	long deleteByOwnerUserIdAndFavoriteUserId(Long ownerUserId, Long favoriteUserId);

	long deleteByFavoriteUserId(Long favoriteUserId);

	@Query("select coalesce(max(f.stepNo),0) from ReceiptApproverFavorite f where f.ownerUserId = ?1")
	int findMaxStepNoByOwner(Long ownerUserId);
}


