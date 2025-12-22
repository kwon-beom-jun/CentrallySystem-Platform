package com.cs.rcpt.controller.request;

import lombok.Data;

@Data
public class DefaultApproverDto {
    private Long userId;
    private String userName;
    private Integer stepNo;
    private String email;
    private String department;
    private String team;
}

