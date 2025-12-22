package com.cs.auth.controller.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRolesResponse {
    
    // 전체 권한 목록
    private List<AuthUserWithRolesResponse.RoleInfo> allRoles;
}
