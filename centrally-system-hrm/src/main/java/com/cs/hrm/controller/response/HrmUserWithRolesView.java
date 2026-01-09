package com.cs.hrm.controller.response;

public interface HrmUserWithRolesView {
    Integer getUserId();
    String getName();
    String getRoles(); // GROUP_CONCAT을 통해 쉼표로 구분된 권한 목록 문자열을 받음
}