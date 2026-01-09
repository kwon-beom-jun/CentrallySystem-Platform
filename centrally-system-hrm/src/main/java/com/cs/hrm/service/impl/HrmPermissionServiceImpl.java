package com.cs.hrm.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.hrm.controller.request.HrmUserPermissionPatchRequest;
import com.cs.hrm.controller.response.HrmRolesResponse;
import com.cs.hrm.controller.response.HrmUserPermissionsResponse;
import com.cs.hrm.controller.response.HrmUserWithRolesResponse;
import com.cs.hrm.entity.HrmRoles;
import com.cs.hrm.entity.HrmUser;
import com.cs.hrm.entity.HrmUserRoles;
import com.cs.hrm.repository.HrmRolesRepository;
import com.cs.hrm.repository.HrmUserRepository;
import com.cs.hrm.service.HrmPermissionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HrmPermissionServiceImpl implements HrmPermissionService {

    private final HrmUserRepository userRepo;
    private final HrmRolesRepository roleRepo;

    @Override
    @Transactional(readOnly = true)
    public HrmUserPermissionsResponse getHrmUserPermissionsResponse() {
        // 1) 모든 사용자 + 권한
        List<HrmUserWithRolesResponse> users = userRepo.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());

        // 2) 모든 권한
        List<HrmUserWithRolesResponse.RoleInfo> allRoles = roleRepo.findAll().stream()
            .map(r -> HrmUserWithRolesResponse.RoleInfo.builder()
                    .roleId(r.getRoleId())
                    .roleName(r.getRoleName())
                    .roleNameDetail(r.getRoleNameDetail())
                    .serviceName(r.getServiceName())
                    .build())
            .collect(Collectors.toList());

        return HrmUserPermissionsResponse.builder()
            .userWithRolesList(users)
            .authRoles(HrmRolesResponse.builder().allRoles(allRoles).build())
            .build();
    }

    @Override
    @Transactional
    public void updateUserPermission(HrmUserPermissionPatchRequest req) {
        HrmUser user = userRepo.findById(req.getUserId())
            .orElseThrow(() -> new IllegalArgumentException(
                GlobalExceptionHandler.CC + "유저가 없습니다: " + req.getUserId()));

        // 같은 서비스 권한 모두 제거
        user.getUserRoles().removeIf(ur ->
            ur.getRole().getServiceName().equals(req.getServiceName()));

        // 새 권한 찾기
        HrmRoles newRole = roleRepo.findAll().stream()
            .filter(r -> r.getServiceName().equals(req.getServiceName())
                      && r.getRoleName().equals(req.getRoleName()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                GlobalExceptionHandler.CC + "권한을 찾을 수 없습니다: "
                  + req.getServiceName() + "/" + req.getRoleName()));

        user.getUserRoles().add(HrmUserRoles.builder()
                .user(user)
                .role(newRole)
                .build());

        userRepo.save(user);
    }

    @Override
    @Transactional
    public void createUserWithRole(HrmUserPermissionPatchRequest req) {
        HrmUser user = userRepo.findById(req.getUserId())
            .orElseThrow(() -> new IllegalArgumentException(
                GlobalExceptionHandler.CC + "유저가 없습니다: " + req.getUserId()));

        boolean exists = user.getUserRoles().stream()
            .anyMatch(ur -> ur.getRole().getServiceName().equals(req.getServiceName()));
        if (exists) throw new IllegalArgumentException(
            GlobalExceptionHandler.CC + "이미 권한이 존재합니다: " + req.getServiceName());

        HrmRoles newRole = roleRepo.findAll().stream()
            .filter(r -> r.getServiceName().equals(req.getServiceName())
                      && r.getRoleName().equals(req.getRoleName()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                GlobalExceptionHandler.CC + "권한을 찾을 수 없습니다"));

        user.getUserRoles().add(HrmUserRoles.builder()
            .user(user)
            .role(newRole)
            .build());
        userRepo.save(user);
    }

    @Override
    @Transactional
    public void deleteUserWithRole(HrmUserPermissionPatchRequest req) {
        HrmUser user = userRepo.findById(req.getUserId())
            .orElseThrow(() -> new IllegalArgumentException(
                GlobalExceptionHandler.CC + "유저가 없습니다: " + req.getUserId()));

        user.getUserRoles().removeIf(ur ->
            ur.getRole().getServiceName().equals(req.getServiceName())
         && ur.getRole().getRoleName().equals(req.getRoleName()));

        userRepo.save(user);
    }

    private HrmUserWithRolesResponse toDto(HrmUser u) {
        List<HrmUserWithRolesResponse.RoleInfo> roles = u.getUserRoles().stream()
            .map(HrmUserRoles::getRole)
            .map(r -> HrmUserWithRolesResponse.RoleInfo.builder()
                .roleId(r.getRoleId())
                .roleName(r.getRoleName())
                .roleNameDetail(r.getRoleNameDetail())
                .serviceName(r.getServiceName())
                .build())
            .collect(Collectors.toList());

        return HrmUserWithRolesResponse.builder()
            .userId(u.getUserId())
            .email(u.getEmail())
            .name(u.getName())
            .roles(roles)
            .build();
    }
}

