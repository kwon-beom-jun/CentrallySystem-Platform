package com.cs.rcpt.repository;

import com.cs.rcpt.entity.ReceiptParticipants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReceiptParticipantsRepository extends JpaRepository<ReceiptParticipants, Integer> {
}
