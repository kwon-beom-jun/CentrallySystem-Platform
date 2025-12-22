package com.cs.hrm.controller.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @Builder
public class HrmUserWithRolesResponse {
    private Integer userId;
    private String email;
    private String name;
    private List<RoleInfo> roles;

    @Data @Builder
    public static class RoleInfo {
        private Integer roleId;
        private String roleName;
        private String roleNameDetail;
        private String serviceName;
    }
}
