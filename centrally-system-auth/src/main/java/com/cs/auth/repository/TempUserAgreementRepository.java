package com.cs.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cs.auth.entity.TempUserAgreement;

public interface TempUserAgreementRepository extends JpaRepository<TempUserAgreement, Long> {
} 