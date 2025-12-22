package com.cs.hrm.controller.request;

import lombok.Data;

@Data
public class HrmUserPermissionPatchRequest {
    private Integer userId;
    private String serviceName;
    private String roleName;        // Enum 코드
}
