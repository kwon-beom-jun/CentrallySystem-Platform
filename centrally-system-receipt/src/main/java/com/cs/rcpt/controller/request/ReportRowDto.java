package com.cs.rcpt.controller.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportRowDto {
    private String name;
    private String department;
    private String team;
    private String email;
    private Integer count;
    private Long approved; // 금액은 Long 타입으로 받는 것이 안전
}