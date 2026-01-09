package com.cs.auth.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs.auth.controller.request.AuthJoinRequest;
import com.cs.auth.controller.response.AuthUserSocialResponse;
import com.cs.auth.controller.response.AuthUserWithRolesResponse;
import com.cs.auth.controller.response.AuthUserWithRolesResponse.RoleInfo;
import com.cs.auth.entity.AuthRoles;
import com.cs.auth.entity.AuthSocialLogin;
import com.cs.auth.entity.AuthUser;
import com.cs.auth.entity.AuthUserRoles;
import com.cs.auth.repository.AuthRolesRepository;
import com.cs.auth.repository.AuthSocialLoginRepository;
import com.cs.auth.repository.AuthUserRepository;
import com.cs.auth.service.AuthUserService;
import com.cs.core.vo.event.AuthUserDeletedEvent;
import com.cs.core.vo.event.AuthUserJoinedEvent;
import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.core.vo.event.AuthUserEnabledChangedEvent;

import com.cs.auth.kafka.producer.AuthUserEventProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

/**
 * AuthUser 관련 CRUD 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class AuthUserServiceImpl implements AuthUserService {

    private final AuthUserRepository authUserRepository;
    private final AuthRolesRepository authRolesRepository;
    private final AuthSocialLoginRepository authSocialLoginRepository;
    private final PasswordEncoder passwordEncoder;
    // Kafka Producer
    private final AuthUserEventProducer authUserEventProducer;
    private final ObjectMapper objectMapper;

    /**
     * 모든 사용자 조회
     */
    @Override
    @Transactional
    public List<AuthUser> getAllUsers() {
        return authUserRepository.findAll();
    }

    /**
     * ID로 사용자 조회
     */
    @Override
    @Transactional
    public Optional<AuthUser> getUserById(Integer id) {
        return authUserRepository.findById(id);
    }

    /**
     * 이메일로 사용자 조회
     */
    @Override
    @Transactional
    public Optional<AuthUser> getUserByEmail(String email) {
        return authUserRepository.findByEmail(email);
    }
    

    /**
     * 사용자 ID로 사용자 정보와 권한 목록 함께 조회
     * @param userId 사용자 ID
     * @return AuthUserWithRolesResponse DTO
     */
    @Override
    @Transactional(readOnly = true)
    public AuthUserWithRolesResponse getUserWithRoles(Integer userId) {
        // 1. ID로 사용자 정보 조회 (N+1 문제 방지를 위해 fetch join 사용 고려 가능)
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "User not found: " + userId));

        // 2. 사용자의 권한 정보를 RoleInfo DTO 리스트로 변환
        List<RoleInfo> roleInfos = user.getUserRoles().stream()
                .map(AuthUserRoles::getRole) // AuthUserRoles 엔티티에서 AuthRoles 엔티티를 추출
                .map(role -> RoleInfo.builder() // AuthRoles 정보를 RoleInfo DTO로 매핑
                        .roleId(role.getRoleId())
                        .roleName(role.getRoleName())
                        .roleNameDetail(role.getRoleNameDetail())
                        .serviceName(role.getServiceName())
                        .build())
                .collect(Collectors.toList());

        // 3. 최종 응답 DTO(AuthUserWithRolesResponse)를 빌더 패턴으로 생성하여 반환
        return AuthUserWithRolesResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                // ※ 참고: AuthUser 엔티티에는 teamId 필드가 없으므로 DTO의 teamId는 null로 채워집니다.
                // .teamId(user.getTeamId()) 
                .roles(roleInfos)
                .build();
    }

    
    /**
     * 회원가입
     */
    @Override
    @Transactional
    public void createUser(AuthJoinRequest request) {
        // 1) 이메일 중복 체크
        if (authUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(GlobalExceptionHandler.CC + "이미 사용 중인 이메일입니다.");
        }

        // 2) AuthUser 엔티티 생성
        AuthUser user = AuthUser.builder()
                .email(request.getEmail())
                // 임시 회원 가입 인코딩 된 비밀번호 그대로 사용
//                .password(passwordEncoder.encode(request.getPassword()))
                .password(request.getPassword())
                .name(request.getUserName())
                .build();

        // 3) 먼저 user를 저장
        authUserRepository.save(user);

        // 4) 가입 이벤트 Outbox 기록
        AuthUserJoinedEvent event = new AuthUserJoinedEvent(
                user.getUserId(), user.getEmail(), user.getPassword(),
                user.getName(), request.getPhone());
        authUserEventProducer.created(event);
    }

    /**
     * putUser (전체 업데이트)
     */
    @Override
    @Transactional
    public void putUser(Integer id, AuthUser request) {
        AuthUser user = authUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "User not found: " + id));

        // 필요한 필드만 업데이트
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        if (!request.getPassword().equals(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setEnabled(request.getEnabled());

        // 저장
        authUserRepository.save(user);
    }

    /**
     * patchUser (일부 업데이트)
     */
    @Override
    @Transactional
    public void patchUser(Integer id, Map<String, Object> patchData) {
        AuthUser user = authUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "User not found: " + id));

        if (patchData.containsKey("name")) {
            user.setName((String) patchData.get("name"));
        }
        if (patchData.containsKey("email")) {
            user.setEmail((String) patchData.get("email"));
        }
        if (patchData.containsKey("password")) {
        	
//            String pw = (String) patchData.get("password");
            // BCrypt 규격 문자열이면 그대로 저장
            // 그렇지 않으면 평문으로 간주 → encode
            // TODO : Auth, Hrm 둘다 BCryptPasswordEncoder 사용해서 가능
//            user.setPassword(isBcrypt(pw) ? pw : passwordEncoder.encode(pw));
            // 인증 서버에서만 비밀번호 관리로 변경
//            user.setPassword(passwordEncoder.encode(pw));
        	
            String newPassword = (String) patchData.get("password");

            // currentPassword가 존재할 때만 검증 로직을 실행
            if (patchData.containsKey("currentPassword")) {
                String currentPassword = (String) patchData.get("currentPassword");
                if (currentPassword == null || currentPassword.isEmpty()) {
                     throw new IllegalArgumentException(GlobalExceptionHandler.CC + "현재 비밀번호를 입력해주세요.");
                }
                if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                    throw new IllegalArgumentException(GlobalExceptionHandler.CC + "현재 비밀번호가 일치하지 않습니다.");
                }
            }
            // currentPassword가 없으면(관리자 모드) 검증을 건너뛰고 바로 새 비밀번호 설정
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        boolean enabledChanged = false;
        if (patchData.containsKey("enabled")) {
            Boolean newEnabled = (Boolean) patchData.get("enabled");
            if (!Objects.equals(user.getEnabled(), newEnabled)) {
                user.setEnabled(newEnabled);
                enabledChanged = true;
            }
        }
        
        authUserRepository.save(user);

        if (enabledChanged) {
            AuthUserEnabledChangedEvent evt = new AuthUserEnabledChangedEvent(user.getUserId(), user.getEnabled());
            authUserEventProducer.enabledChanged(evt);
        }
    }
    
    /**
     * 삭제
     */
    @Override
    @Transactional
    public void deleteUser(Integer id) {

        AuthUser user = authUserRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "User not found: " + id));

        // ① Soft-Delete 실행 (enabled=false 로 UPDATE)
        authUserRepository.delete(user);

        // ② Outbox 이벤트 기록 -------------------------------
        AuthUserDeletedEvent deletedEvent = new AuthUserDeletedEvent(user.getUserId(), user.getEmail());
        authUserEventProducer.deactivated(deletedEvent);
    }


    // ========================== 소셜 로그인 예시 ==========================
    @Override
    public Optional<AuthUserSocialResponse> getUserSocialByEmail(String email) {
        Optional<AuthUser> userOpt = authUserRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            AuthUser user = userOpt.get();
            List<AuthSocialLogin> socialLogins = authSocialLoginRepository.findByUserId(user.getUserId());
            return Optional.of(new AuthUserSocialResponse(user, socialLogins));
        }
        return Optional.empty();
    }

    @Override
    @Transactional
    public void deleteUserSocial(String userId, String socialName) {
        Optional<AuthSocialLogin> socialLoginOpt = authSocialLoginRepository.findByUserIdAndSocialName(Integer.valueOf(userId), socialName);
        AuthSocialLogin socialLogin = socialLoginOpt
                .orElseThrow(() -> new RuntimeException(
                        GlobalExceptionHandler.CC + "소셜 정보가 존재하지 않습니다. userId=" + userId + ", socialName=" + socialName
                ));
        authSocialLoginRepository.delete(socialLogin);
    }
    
    
    
    /* helper */
    private String toJson(Object o) {
        try { return objectMapper.writeValueAsString(o); }
        catch (JsonProcessingException e) { throw new IllegalStateException(e); }
    }
    private static boolean isBcrypt(String s) {
        if (s == null) return false;
        // "{bcrypt}" 접두 포맷 (Spring DelegatingPasswordEncoder)
        if (s.startsWith("{bcrypt}") && s.length() == 68) return true;
        // 표준 BCrypt 포맷
        return (s.startsWith("$2a$") || s.startsWith("$2b$") || s.startsWith("$2y$"))
                && s.length() == 60;
    }
}

