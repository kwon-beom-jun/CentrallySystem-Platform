package com.cs.auth.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cs.auth.entity.Agreement;
import com.cs.auth.service.AgreementService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/agreements")
@RequiredArgsConstructor
public class AgreementController {

    private final AgreementService agreementService;

    /**
     * 현재 적용 중인 약관 목록 반환
     */
    @GetMapping
    public List<Agreement> getAgreements(@RequestParam(value = "lang", required = false) String languageCode) {
        return agreementService.getLatestAgreements(languageCode);
    }
} 