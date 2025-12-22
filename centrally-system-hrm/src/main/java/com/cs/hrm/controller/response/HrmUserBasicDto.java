package com.cs.hrm.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HrmUserBasicDto {

    private final Integer userId;
    private final String  name;
    private final String  email;
    private final String  department;   // 부서명
    private final String  team;         // 팀명
}