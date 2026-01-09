package com.cs.hrm.util;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

import com.cs.core.enums.Role;

/**
 * 권한 체크 유틸리티 (HRM 도메인 전용)
 */
public final class RoleUtils {

    private RoleUtils() {}

    /**
     * 지정한 권한 보유 여부
     */
    public static boolean hasRole(Collection<? extends GrantedAuthority> authorities, String role) {
        if (authorities == null || authorities.isEmpty() || role == null) return false;
        for (GrantedAuthority a : authorities) {
            if (role.equals(a.getAuthority())) return true;
        }
        return false;
    }

    /**
     * HRM 관리자 여부
     */
    public static boolean isHrmManager(Collection<? extends GrantedAuthority> authorities) {
        return hasRole(authorities, Role.ROLE_HRM_MANAGER.authority());
    }
}


