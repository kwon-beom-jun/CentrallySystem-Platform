package com.cs.rcpt.controller.request;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BulkUsersActionRequest {
    private List<Integer> userIds;      // 신청자 PK 목록 (필수)
    private LocalDate     startDate;    // YYYY-MM-01
    private LocalDate     endDate;      // YYYY-MM-DD (말일)
    private String        reason;       // 반려 사유 (승인은 null)
    private List<Integer> roles;    // ex) [1] 은 “결재”, [2,3] 은 “합의”
}

