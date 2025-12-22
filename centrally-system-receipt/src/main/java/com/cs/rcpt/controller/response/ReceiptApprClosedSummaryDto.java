package com.cs.rcpt.controller.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReceiptApprClosedSummaryDto {
	
	/* 신청자 정보 ― 스냅샷 */
    private Integer userId;   // 신청자 ID
    private String  userName;
    private String  userEmail;
    private Integer departmentId;
    private String  departmentName;
    private Integer teamId;
    private String  teamName;
    
    /* 집계 값 */
    private Long    count;    // 총 건수(= 승인+마감)
    private Long    approved; // 승인 금액 합
    private Long    closed;   // 마감 금액 합
}
