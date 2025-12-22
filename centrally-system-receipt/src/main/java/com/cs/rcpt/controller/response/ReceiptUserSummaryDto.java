package com.cs.rcpt.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptUserSummaryDto {
    private Integer userId;     // 신청자
    private String  userName;
    private String  userEmail;
    private Integer departmentId;
    private String  departmentName;
    private Integer teamId;
    private String  teamName;
    private Long    count;      // 총 건수
    private Long    waiting;    // WAITING
    private Long    requested;  // REQUEST
    private Long    approved;   // APPROVED
    private Long    rejected;   // REJECTED
    private Long    closed;     // CLOSED
}
