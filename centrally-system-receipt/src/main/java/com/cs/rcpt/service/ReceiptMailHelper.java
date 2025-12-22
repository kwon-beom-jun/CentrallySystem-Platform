package com.cs.rcpt.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.cs.core.service.EmailService;
import com.cs.rcpt.entity.Receipt;
import com.cs.rcpt.entity.ReceiptApprovalLine;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReceiptMailHelper {

    private final EmailService emailService;

    /**
     * 다음 결재자에게 메일 발송
     */
    public void notifyNextApprovers(Receipt receipt) {
        // 포트폴리오용 - 구현 제거됨
        // 기능: 다음 결재자에게 메일 알림 발송
        // - 현재 결재 단계의 결재자 이메일 수집
        // - 메일 제목 및 본문 생성
        // - EmailService를 통한 메일 발송
    }

    /**
     * 영수증 신청 시 첫 번째 결재자(현재 currentApprovalStep==1)에게 알림 메일 발송
     */
    public void notifySubmissionFirstApprovers(Receipt receipt) {
        // 포트폴리오용 - 구현 제거됨
        // 기능: 영수증 신청 시 첫 번째 결재자에게 알림 메일 발송
        // - 첫 번째 결재 단계(step=1)의 결재자 이메일 수집
        // - 상세한 메일 본문 생성 (영수증 정보 포함)
        // - EmailService를 통한 메일 발송
    }

    /**
     * 반려 시 관련자(기승인자 + 제출자)에게 메일 발송
     */
    public void notifyReject(Receipt receipt, String reason) {
        // 포트폴리오용 - 구현 제거됨
        // 기능: 영수증 반려 시 관련자에게 메일 알림 발송
        // - 기승인자 목록 수집 (합의자 제외, 대리결재 고려)
        // - 기승인자에게 반려 통보 메일 발송
        // - 제출자에게 반려 안내 메일 발송
        // - 메일 본문 생성 (결재자용/제출자용 구분)
    }

    /* 반려 메일 본문 생성 -------------------------------------------------- */
    private String buildRejectMailText(Receipt r, String reason, boolean forSubmitter) {
        // 포트폴리오용 - 구현 제거됨
        // 기능: 반려 메일 본문 생성
        // - 제출자용/결재자용 메일 본문 구분 생성
        // - 영수증 정보 및 반려 사유 포함
        return "";
    }
} 