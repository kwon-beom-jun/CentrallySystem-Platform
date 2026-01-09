package com.cs.rcpt.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cs.rcpt.entity.ReceiptCodeSeq;

import jakarta.persistence.LockModeType;

public interface ReceiptCodeSeqRepository extends JpaRepository<ReceiptCodeSeq,String>{

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ReceiptCodeSeq s where s.yymm = :yymm")
    Optional<ReceiptCodeSeq> findLock(@Param("yymm") String yymm);
}

