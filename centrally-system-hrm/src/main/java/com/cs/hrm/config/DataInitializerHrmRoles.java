package com.cs.hrm.config;

import com.cs.hrm.entity.*;
import com.cs.hrm.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.*;

/**
 *  Auth ↔ HRM  “Role / User-Role”  동기화용 (읽기 전용 캐시)
 *
 *  - HRM 은 권한을 직접 쓰지 않으므로 INSERT 만 해두고
 *  - 수정 / 삭제는 Auth 쪽 이벤트(or 배치)로 동기화하는 것을 전제로 한다.
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializerHrmRoles {

    private final HrmRolesRepository       roleRepo;
    private final HrmUserRepository        userRepo;
    private final HrmUserRolesRepository   userRoleRepo;

    /** Auth 서비스와 **똑같이** 유지할 Role Seed */
    private static final List<RoleSeed> ROLE_SEEDS = List.of(
        new RoleSeed("ROLE_HRM_EMPLOYEE" , "사원"    , "hrm"),
        new RoleSeed("ROLE_HRM_LEADER"  , "리더"    , "hrm"),
        new RoleSeed("ROLE_HRM_ASSISTANT_MANAGER"  , "중간관리자"  , "hrm"),
        new RoleSeed("ROLE_HRM_MANAGER"  , "관리자"  , "hrm"),

        new RoleSeed("ROLE_RECEIPT_REGISTRAR" , "등록자"  , "receipt"),
        new RoleSeed("ROLE_RECEIPT_APPROVER"  , "결재자"  , "receipt"),
        new RoleSeed("ROLE_RECEIPT_INSPECTOR" , "검수자"  , "receipt"),
        new RoleSeed("ROLE_RECEIPT_FINALIZER" , "확정자"  , "receipt"),
        new RoleSeed("ROLE_RECEIPT_MANAGER"   , "관리자"  , "receipt"),
        
        new RoleSeed("ROLE_INFO_GUEST"        , "게스트"  , "info"),
        new RoleSeed("ROLE_INFO_USER"         , "사용자"  , "info"),
        new RoleSeed("ROLE_INFO_MANAGER"      , "운영자"  , "info"),
        new RoleSeed("ROLE_INFO_ADMIN"        , "관리자"  , "info"),
        
        new RoleSeed("ROLE_GATE_SYSTEM"       , "시스템"  , "system")
    );

    /** 사용자-역할 매핑 Seed  (Auth DataInitializer 의 createUserIfNotExists 와 동일하게 "test"/"system") */
    private static final List<UserRoleSeed> USER_ROLE_SEEDS = List.of(
        new UserRoleSeed("test", List.of(
            "ROLE_HRM_MANAGER",
            "ROLE_RECEIPT_INSPECTOR",
            "ROLE_INFO_ADMIN",
            "ROLE_GATE_SYSTEM"
        )),
        new UserRoleSeed("system", List.of(
            "ROLE_HRM_EMPLOYEE",
            "ROLE_RECEIPT_APPROVER",
            "ROLE_INFO_USER",
            "ROLE_GATE_SYSTEM"
        ))
        // user1~user12 은 아래 2-2 절에서 처리
    );

    /**
     * 포트폴리오용: 데이터 초기화 기능 비활성화
     */

    /* ---------------- 내부 util ---------------- */
    private void insertIfNotExists(HrmUser user, HrmRoles role) {
        boolean exists = userRoleRepo
            .existsByUserUserIdAndRoleRoleId(user.getUserId(), role.getRoleId());
        if (!exists) {
            userRoleRepo.save(
                HrmUserRoles.builder()
                            .user(user)
                            .role(role)
                            .build());
        }
    }

    /* ------ record seeds ------ */
    private record RoleSeed(String roleName, String roleNameDetail, String service) {}
    private record UserRoleSeed(String email, List<String> roleNames) {}
}
