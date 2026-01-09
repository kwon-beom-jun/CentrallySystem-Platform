package com.cs.auth.service.impl;

import com.cs.auth.controller.request.AuthUserPermissionPatchRequest;
import com.cs.auth.controller.response.AuthRolesResponse;
import com.cs.auth.controller.response.AuthUserPermissionsResponse;
import com.cs.auth.controller.response.AuthUserWithRolesResponse;
import com.cs.auth.entity.AuthRoles;
import com.cs.auth.entity.AuthUser;
import com.cs.auth.entity.AuthUserRoles;
import com.cs.auth.repository.AuthRolesRepository;
import com.cs.auth.repository.AuthUserRepository;
import com.cs.auth.kafka.producer.AuthPermissionEventProducer;
import com.cs.auth.service.AuthPermissionService;
import com.cs.core.handler.GlobalExceptionHandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthPermissionServiceImpl implements AuthPermissionService {

    private final AuthUserRepository authUserRepository;
    private final AuthRolesRepository authRolesRepository;
    private final AuthPermissionEventProducer authPermissionEventProducer;

    /**
     * 유저별 권한 정보 + 전체 권한 목록
     */
    @Override
    @Transactional
    public AuthUserPermissionsResponse getAuthUserPermissionsResponse() {
        // 1) 전체 유저
        List<AuthUser> userList = authUserRepository.findAll();
        List<AuthUserWithRolesResponse> userWithRolesList = userList.stream()
                .map(this::mapToAuthUserWithRolesResponse)
                .collect(Collectors.toList());
        
        // 2) 전체 권한
        List<AuthRoles> roleList = authRolesRepository.findAll();
        List<AuthUserWithRolesResponse.RoleInfo> allRoleInfoList = roleList.stream()
                .map(this::mapToRoleInfoDto)
                .collect(Collectors.toList());
        
        // 3) AuthRolesResponse
        AuthRolesResponse authRolesResponse = AuthRolesResponse.builder()
                .allRoles(allRoleInfoList)
                .build();
        
        // 4) 최종 응답
        return AuthUserPermissionsResponse.builder()
                .userWithRolesList(userWithRolesList)
                .authRoles(authRolesResponse)
                .build();
    }

    /**
     * 유저별 권한 정보 업데이트
     */
    @Override
    @Transactional
    public void updateUserPermission(AuthUserPermissionPatchRequest request) {
        // 1) 유저 조회
        AuthUser user = authUserRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                		GlobalExceptionHandler.CC + "해당 유저가 존재하지 않습니다. userId=" + request.getUserId()
            		));

        // 2) 기존 해당 서비스 권한 제거
        user.getUserRoles().removeIf(
                ur -> ur.getRole().getServiceName().equals(request.getServiceName())
        );

        // 3) 새 Role 찾기
        AuthRoles newRole = authRolesRepository.findAll().stream()
                .filter(r -> r.getServiceName().equals(request.getServiceName())
                          && r.getRoleName().equals(request.getRoleName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                		GlobalExceptionHandler.CC
                		+ "해당 권한(서비스=" + request.getServiceName() + ", role=" + request.getRoleName() + ")이 "
        				+ "존재하지 않습니다."
                ));

        // 4) 새 AuthUserRoles 추가
        AuthUserRoles newUserRole = AuthUserRoles.builder()
                .user(user)
                .role(newRole)
                .build();
        user.getUserRoles().add(newUserRole);

        // 5) 저장
        authUserRepository.save(user);
        
        // 권한 변경 이벤트 Outbox 기록
        authPermissionEventProducer.updated(request);
    }

    /**
     * 유저 권한 추가
     */
    @Override
    @Transactional
    public void createUserWithRole(AuthUserPermissionPatchRequest request) {
        AuthUser user = authUserRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                		GlobalExceptionHandler.CC + "유저 없음: " + request.getUserId()
            		));

        boolean alreadyHasRole = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getServiceName().equals(request.getServiceName()));

        if (alreadyHasRole) {
            throw new IllegalArgumentException(
            		GlobalExceptionHandler.CC + "이미 해당 서비스 권한이 존재합니다: " + request.getServiceName()
        		);
        }

        AuthRoles newRole = authRolesRepository.findAll().stream()
                .filter(r -> r.getServiceName().equals(request.getServiceName())
                          && r.getRoleName().equals(request.getRoleName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(GlobalExceptionHandler.CC + "권한 없음"));

        user.getUserRoles().add(AuthUserRoles.builder().user(user).role(newRole).build());
        authUserRepository.save(user);
        
        // 권한 추가 이벤트 Outbox 기록
        authPermissionEventProducer.created(request);
    }

    /**
     * 유저 권한 삭제
     */
    @Override
    @Transactional
    public void deleteUserWithRole(AuthUserPermissionPatchRequest request) {
        AuthUser user = authUserRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(GlobalExceptionHandler.CC + "유저 없음: " + request.getUserId()));

        user.getUserRoles().removeIf(
                ur -> ur.getRole().getServiceName().equals(request.getServiceName())
                      && ur.getRole().getRoleName().equals(request.getRoleName())
        );

        authUserRepository.save(user);
        
        // 권한 삭제 이벤트 Outbox 기록
        authPermissionEventProducer.deleted(request);
    }

    /**
     * AuthUser -> DTO 변환
     */
    /* ------------------------- DTO 매핑 ------------------------- */
    private AuthUserWithRolesResponse mapToAuthUserWithRolesResponse(AuthUser user) {
        List<AuthUserWithRolesResponse.RoleInfo> roles = user.getUserRoles().stream()
                .map(AuthUserRoles::getRole)
                .map(this::mapToRoleInfoDto)
                .collect(Collectors.toList());

        return AuthUserWithRolesResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .roles(roles)
                .build();
    }
    
    /**
     * AuthRoles -> DTO 변환
     */
    private AuthUserWithRolesResponse.RoleInfo mapToRoleInfoDto(AuthRoles role) {
        return AuthUserWithRolesResponse.RoleInfo.builder()
                .roleId(role.getRoleId())
                .roleName(role.getRoleName())
                .roleNameDetail(role.getRoleNameDetail())
                .serviceName(role.getServiceName())
                .build();
    }
}

