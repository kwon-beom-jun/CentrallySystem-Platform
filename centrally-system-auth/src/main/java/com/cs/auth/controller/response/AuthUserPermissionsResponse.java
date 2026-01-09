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
public class AuthUserPermissionsResponse {
	
    // 유저별 권한 정보 리스트
    private List<AuthUserWithRolesResponse> userWithRolesList;
    
    // 전체 권한 목록 정보
    private AuthRolesResponse authRoles;

}
