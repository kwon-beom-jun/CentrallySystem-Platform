package com.cs.rcpt.controller.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserDeptTeamUpdateRequest {
    private Integer departmentId;  // NULL 가능
    private String  departmentName;
    private Integer teamId;        // NULL 가능
    private String  teamName;
}
