package com.cs.rcpt.controller.request;

import lombok.Data;

@Data
public class ReceiptDecisionRequest {
    private Integer receiptId;     // 필수
    private Integer statusCode;    // 2-승인, 3-반려
    private Integer approverId;    // 결재선 식별용
    private String approverName;    // 결재선 식별용
    private String  rejectReason;  // 반려 사유(선택)
}
