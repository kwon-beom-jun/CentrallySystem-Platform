//package com.cs.auth.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import com.cs.auth.entity.*;
//import com.cs.auth.repository.AuthRolesRepository;
//import com.cs.auth.repository.AuthUserRepository;
//import com.cs.auth.repository.AuthApplicationRepository;
//import com.cs.auth.repository.AuthUserApplicationRepository;
//
//import java.util.*;
//
///**
// * 애플리케이션 시작 시점에 더미 유저, 롤(Role), 어플리케이션 데이터를 초기화하는 설정 클래스입니다.
// * (더미 유저 '3,000' 생성 20분 소요)
// * 
// * 더미 유저 : 1,000 
// * 
// */
//@Configuration
//@RequiredArgsConstructor
//public class DataInitializer {
//
//    private final AuthUserRepository userRepository;
//    private final AuthRolesRepository roleRepository;
//    private final AuthApplicationRepository applicationRepository;
//    private final AuthUserApplicationRepository userApplicationRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Bean
//    CommandLineRunner initData() {
//        return args -> {
//            // 1) 어플리케이션 데이터 초기화
//            List<String> applicationNames = List.of("영수증 관리", "개인정보 관리", "문서 관리", "시스템 관리");
//            for (String appName : applicationNames) {
//                createApplicationIfNotFound(appName);
//            }
//
//            // 2) 롤(Role) 데이터 초기화
//            AuthRoles roleUser            = createRoleIfNotFound("ROLE_HRM_EMPLOYEE", "사원", "hrm");
//            AuthRoles roleHrmManager      = createRoleIfNotFound("ROLE_HRM_MANAGER", "관리자", "hrm");
//            AuthRoles roleReceiptREGISTRAR= createRoleIfNotFound("ROLE_RECEIPT_REGISTRAR", "등록자", "receipt");
//            AuthRoles roleReceiptApprover = createRoleIfNotFound("ROLE_RECEIPT_APPROVER", "결재자", "receipt");
//            AuthRoles roleReceiptCloser   = createRoleIfNotFound("ROLE_RECEIPT_MANAGER", "관리자", "receipt");
//            AuthRoles roleSystem          = createRoleIfNotFound("ROLE_GATE_SYSTEM", "시스템", "system");
//
//            // 3) 관리자/시스템/매니저 샘플 유저 생성
//            //    (파라미터 마지막에 쉼표 제거!)
//            AuthUser testUser    = createUserIfNotExists("test", "관리자", roleHrmManager, roleReceiptCloser, roleSystem);
//            AuthUser managerUser = createUserIfNotExists("qjawns0618@gmail.com", "권매니저", roleUser, roleReceiptREGISTRAR);
//            AuthUser systemUser  = createUserIfNotExists("system", "시스템", roleUser, roleSystem, roleReceiptApprover);
//
//            // 4) 일반 유저들 3,000명 생성(여기서는 1,000개씩)
//            final int TOTAL_COUNT = 1_000;
//            final int CHUNK_SIZE = 100; // 또는 100
//            List<AuthUser> chunk = new ArrayList<>(CHUNK_SIZE);
//
//            for (int i = 1; i <= TOTAL_COUNT; i++) {
//                String email = String.format("batchAuth%05d@gmail.com", i);
//                String name = "Auth유저_" + i;
//
//                Optional<AuthUser> existing = userRepository.findByEmail(email);
//                if (existing.isEmpty()) {
//                    AuthUser user = AuthUser.builder()
//                            .email(email)
//                            .password(passwordEncoder.encode("1234"))  // 더미 비번
//                            .name(name)
//                            .enabled(true)
//                            .build();
//
//                    // roleUser 할당
//                    AuthUserRoles userRole = AuthUserRoles.builder()
//                            .user(user)
//                            .role(roleUser)
//                            .build();
//                    user.getUserRoles().add(userRole);
//
//                    chunk.add(user);
//                }
//
//                // 1,000개씩 insert
//                if (i % CHUNK_SIZE == 0) {
//                    userRepository.saveAll(chunk);
//                    chunk.clear();
//                }
//            }
//
//            // 남은 거 처리
//            if (!chunk.isEmpty()) {
//                userRepository.saveAll(chunk);
//                chunk.clear();
//            }
//
//            // 5) 유저 → 어플리케이션 할당
//            assignApplicationsToUser(testUser,    "영수증 관리", "개인정보 관리");
//            assignApplicationsToUser(managerUser, "문서 관리");
//            assignApplicationsToUser(systemUser,  "시스템 관리");
//
//            System.out.println("✅ [DataInitializer] Auth 유저 1,000명 (Chunk) 더미 데이터 삽입 완료!");
//        };
//    }
//
//    /**
//     * 어플리케이션이 존재하지 않으면 생성
//     */
//    private void createApplicationIfNotFound(String applicationName) {
//        if (!applicationRepository.existsByApplicationName(applicationName)) {
//            AuthApplication application = AuthApplication.builder()
//                    .applicationName(applicationName)
//                    .build();
//            applicationRepository.save(application);
//        }
//    }
//
//    /**
//     * Role이 존재하지 않으면 생성
//     */
//    private AuthRoles createRoleIfNotFound(String roleName, String roleNameDetail, String serviceName) {
//        // 만약 findByRoleName(...)가 없다면 아래처럼 "커스텀" 코드 사용 가능
//        // return roleRepository.findAll().stream()
//        //         .filter(r -> r.getRoleName().equals(roleName))
//        //         .findFirst()
//        //         .orElseGet(() -> { ...생성... });
//        
//        // 하지만 있는 경우:
//        Optional<AuthRoles> opt = roleRepository.findByRoleName(roleName);
//        if (opt.isPresent()) {
//            return opt.get();
//        } else {
//            AuthRoles newRole = AuthRoles.builder()
//                    .roleName(roleName)
//                    .roleNameDetail(roleNameDetail)
//                    .serviceName(serviceName)
//                    .build();
//            return roleRepository.save(newRole);
//        }
//    }
//
//    /**
//     * 유저가 존재하지 않으면 생성
//     */
//    private AuthUser createUserIfNotExists(String email, String name, AuthRoles... roles) {
//        Optional<AuthUser> existingUser = userRepository.findByEmail(email);
//        if (existingUser.isPresent()) {
//            return existingUser.get();
//        }
//
//        AuthUser user = AuthUser.builder()
//                .email(email)
//                .password(passwordEncoder.encode("1234")) // 기본 비번
//                .name(name)
//                .enabled(true)
//                .build();
//
//        // Roles 할당
//        for (AuthRoles role : roles) {
//            AuthUserRoles userRole = AuthUserRoles.builder()
//                    .user(user)
//                    .role(role)
//                    .build();
//            user.getUserRoles().add(userRole);
//        }
//
//        return userRepository.save(user);
//    }
//
//    /**
//     * 유저에게 특정 어플리케이션 할당
//     */
//    private void assignApplicationsToUser(AuthUser user, String... applicationNames) {
//        for (String appName : applicationNames) {
//            Optional<AuthApplication> appOpt = applicationRepository.findByApplicationName(appName);
//            if (appOpt.isPresent()) {
//                AuthApplication application = appOpt.get();
//                boolean alreadyAssigned = userApplicationRepository
//                        .findByUserUserIdAndApplicationApplicationId(user.getUserId(), application.getApplicationId())
//                        .isPresent();
//
//                if (!alreadyAssigned) {
//                    AuthUserApplication userApp = AuthUserApplication.builder()
//                            .user(user)
//                            .application(application)
//                            .build();
//                    userApplicationRepository.save(userApp);
//                }
//            }
//        }
//    }
//}


