package com.cs.rcpt.controller.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReceiptReportPageDto {
    // 기존 페이징 데이터
    private ReceiptPage<ReceiptApprClosedSummaryDto> pageData;
    // 과거에 승인된 월 목록 (예: ["2025-06", "2025-05"])
    private List<String> pastApprovedMonths;
}