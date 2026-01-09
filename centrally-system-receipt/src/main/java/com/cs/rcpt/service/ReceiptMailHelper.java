package com.cs.rcpt.service;

import com.cs.rcpt.entity.Receipt;

/**
 * 영수증 메일 발송 헬퍼 서비스 인터페이스
 */
public interface ReceiptMailHelper {

    /**
     * 다음 결재자에게 메일 발송
     * @param receipt 영수증 엔티티
     */
    void notifyNextApprovers(Receipt receipt);

    /**
     * 영수증 신청 시 첫 번째 결재자(현재 currentApprovalStep==1)에게 알림 메일 발송
     * @param receipt 영수증 엔티티
     */
    void notifySubmissionFirstApprovers(Receipt receipt);

    /**
     * 반려 시 관련자(기승인자 + 제출자)에게 메일 발송
     * @param receipt 영수증 엔티티
     * @param reason 반려 사유
     */
    void notifyReject(Receipt receipt, String reason);
}
