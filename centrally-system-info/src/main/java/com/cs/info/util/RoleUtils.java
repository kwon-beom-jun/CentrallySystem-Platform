package com.cs.info.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import com.cs.core.enums.Role;
import com.cs.info.enums.VisibilityScope;

/**
 * 권한 체크 유틸리티 (INFO 도메인 전용)
 */
public final class RoleUtils {

    private RoleUtils() {}

    /**
     * 지정한 권한(들) 중 하나라도 보유 여부
     */
    public static boolean hasAnyRole(Collection<? extends GrantedAuthority> authorities, Role... roles) {
        if (authorities == null || authorities.isEmpty() || roles == null) return false;
        for (Role role : roles) {
            if (role == null) continue;
            String roleAuthority = role.authority();
            for (GrantedAuthority a : authorities) {
                if (roleAuthority.equals(a.getAuthority())) return true;
            }
        }
        return false;
    }

    /**
     * INFO 관리자 또는 시스템 관리자 여부
     */
    public static boolean isAdmin(Collection<? extends GrantedAuthority> authorities) {
        return hasAnyRole(authorities, Role.ROLE_INFO_ADMIN, Role.ROLE_GATE_SYSTEM);
    }

    /**
     * INFO 운영자 이상 여부 (MANAGER 이상)
     */
    public static boolean isManager(Collection<? extends GrantedAuthority> authorities) {
        return hasAnyRole(authorities, Role.ROLE_INFO_MANAGER, Role.ROLE_INFO_ADMIN, Role.ROLE_GATE_SYSTEM);
    }

    /**
     * INFO 일반 사용자 이상 여부 (USER 이상)
     */
    public static boolean isUser(Collection<? extends GrantedAuthority> authorities) {
        return hasAnyRole(authorities, Role.ROLE_INFO_USER, Role.ROLE_INFO_MANAGER, Role.ROLE_INFO_ADMIN, Role.ROLE_GATE_SYSTEM);
    }

    /**
     * 사용자 권한에 따라 열람 가능한 VisibilityScope 리스트 반환
     * - ADMIN/SYSTEM: null 반환 (모든 게시글 조회)
     * - MANAGER: ALL, USER_ABOVE, MANAGER_ABOVE
     * - USER: ALL, USER_ABOVE
     * - GUEST: ALL
     */
    public static List<VisibilityScope> getAllowedVisibilityScopes(Collection<? extends GrantedAuthority> authorities) {
        if (isAdmin(authorities)) {
            return null; // 관리자는 필터 없이 전체 조회
        }
        
        List<VisibilityScope> scopes = new ArrayList<>();
        
        if (isManager(authorities)) {
            scopes.add(VisibilityScope.ALL);
            scopes.add(VisibilityScope.USER_ABOVE);
            scopes.add(VisibilityScope.MANAGER_ABOVE);
        } else if (isUser(authorities)) {
            scopes.add(VisibilityScope.ALL);
            scopes.add(VisibilityScope.USER_ABOVE);
        } else {
            scopes.add(VisibilityScope.ALL);
        }
        
        return scopes;
    }

    /**
     * 특정 VisibilityScope에 접근 가능한지 확인
     */
    public static boolean canAccessVisibilityScope(Collection<? extends GrantedAuthority> authorities, VisibilityScope scope) {
        if (scope == null) return true;
        if (isAdmin(authorities)) return true;
        
        switch (scope) {
            case ALL:
                return true; // 모든 사용자 접근 가능
            case USER_ABOVE:
                return isUser(authorities);
            case MANAGER_ABOVE:
                return isManager(authorities);
            default:
                return false;
        }
    }
}

