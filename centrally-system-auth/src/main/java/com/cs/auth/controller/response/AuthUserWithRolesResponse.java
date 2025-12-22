package com.cs.auth.controller.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUserWithRolesResponse {
    
    private Integer userId;
    private String email;
    private String name;
    private Integer teamId;

    // 유저가 가진 권한 목록
    private List<RoleInfo> roles;

    // 내부 클래스: 각 권한의 상세 정보를 담는 DTO
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoleInfo {
        private Integer roleId;
        private String roleName;         // 권한 이름
        private String roleNameDetail;   // 권한 상세 이름
        private String serviceName;      // 서비스 이름
    }
}
