package com.cs.auth.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cs.auth.entity.Agreement;

public interface AgreementRepository extends JpaRepository<Agreement, Long> {
    List<Agreement> findByLanguageCode(String languageCode);

    List<Agreement> findByLanguageCodeAndIsRequiredTrue(String languageCode);
}