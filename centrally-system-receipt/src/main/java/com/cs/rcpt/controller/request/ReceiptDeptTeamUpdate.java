package com.cs.rcpt.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReceiptDeptTeamUpdate {

    private Integer departmentId;
    private String departmentName;
    private Integer teamId;
    private String teamName;
}