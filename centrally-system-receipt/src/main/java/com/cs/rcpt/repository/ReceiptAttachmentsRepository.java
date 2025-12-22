package com.cs.rcpt.repository;

import com.cs.rcpt.entity.ReceiptAttachments;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptAttachmentsRepository extends JpaRepository<ReceiptAttachments, Integer> {
	
	Optional<ReceiptAttachments> findByFileOriginName(String fileOriginName);
}
