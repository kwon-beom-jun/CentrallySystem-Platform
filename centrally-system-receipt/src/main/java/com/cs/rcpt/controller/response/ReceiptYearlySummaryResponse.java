package com.cs.rcpt.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 연도별 영수증 요약 전체 응답
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReceiptYearlySummaryResponse {

    private int year;
    private List<MonthlySummary> monthlyList;

    /* 연간 합계 */
    private long totalWaiting;
    private long totalRequest;
    private long totalApproved;
    private long totalRejected;
    private long totalClosed;
    private long totalSum;

    /* ───── 월별 세부 ───── */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MonthlySummary {
        private int  month;     // 1~12
        private long waiting;   // 4 = WAITING
        private long request;   // 1 = REQUEST
        private long approved;  // 2 = APPROVED
        private long rejected;  // 3 = REJECTED
        private long closed;    // 5 = CLOSED
        private long sum;       // 월 전체
    }
}
