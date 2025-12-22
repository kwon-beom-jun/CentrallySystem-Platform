package com.cs.hrm.controller.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @Builder
public class HrmRolesResponse {
    private List<HrmUserWithRolesResponse.RoleInfo> allRoles;
}
