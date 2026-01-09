package com.cs.auth.controller.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthUserPermissionPatchRequest {
    private Integer userId;          // 유저 ID
    private String serviceName;      // 예: "hrm", "receipt", "system"
    private String roleName;         // 예: "ROLE_HRM_MANAGER" 등 (Enum 코드)
}
