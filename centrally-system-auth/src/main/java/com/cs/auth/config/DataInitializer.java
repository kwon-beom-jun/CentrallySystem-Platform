package com.cs.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cs.auth.entity.*;
import com.cs.auth.repository.AuthRolesRepository;
import com.cs.auth.repository.AuthUserRepository;
import com.cs.core.domain.ProcessingStatus;
import com.cs.auth.repository.AuthApplicationRepository;
import com.cs.auth.repository.AuthUserApplicationRepository;
import com.cs.auth.repository.AgreementRepository;
import com.cs.auth.entity.Agreement;
import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

/**
 * 애플리케이션 시작 시점에 더미 유저, 롤(Role), 어플리케이션 데이터를 초기화하는 설정 클래스입니다.
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final AuthUserRepository userRepository;
    private final AuthRolesRepository roleRepository;
    private final AuthApplicationRepository applicationRepository;
    private final AuthUserApplicationRepository userApplicationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AgreementRepository agreementRepository;

    /**
     * 포트폴리오용: 데이터 초기화 기능 비활성화
     */

    /**
     * 어플리케이션이 존재하지 않으면 생성하는 메서드
     */
    private void createApplicationIfNotFound(String applicationName) {
        if (!applicationRepository.existsByApplicationName(applicationName)) {
            AuthApplication application = AuthApplication.builder()
                    .applicationName(applicationName)
                    .build();
            applicationRepository.save(application);
        }
    }

    /**
     * 역할(Role)이 존재하지 않으면 생성하는 메서드
     */
    private AuthRoles createRoleIfNotFound(String roleName, String roleNameDetail, String serviceName) {
        return roleRepository.findAll().stream()
                .filter(r -> r.getRoleName().equals(roleName))
                .findFirst()
                .orElseGet(() -> {
                    AuthRoles newRole = AuthRoles.builder()
                            .roleName(roleName)
                            .roleNameDetail(roleNameDetail)
                            .serviceName(serviceName)
                            .build();
                    return roleRepository.save(newRole);
                });
    }

    /**
     * 유저가 존재하지 않으면 생성 (간소화된 예시)
     */
    private AuthUser createUserIfNotExists(String email, String name, AuthRoles... roles) {
        Optional<AuthUser> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            // 이미 존재하면 그대로 반환
            return existingUser.get();
        }

        // 새 유저 엔티티 생성
        AuthUser user = AuthUser.builder()
                .email(email)
                .password(passwordEncoder.encode("1234")) // 모든 더미 유저 비번 = "1234"
                .name(name)
                .status(ProcessingStatus.ACTIVE)
                .build();

        // 유저에게 권한(roles) 할당
        for (AuthRoles role : roles) {
            AuthUserRoles userRole = AuthUserRoles.builder()
                    .user(user)
                    .role(role)
                    .build();
            user.getUserRoles().add(userRole);
        }

        return userRepository.save(user);
    }

    /**
     * 유저에게 특정 어플리케이션을 할당
     */
    private void assignApplicationsToUser(AuthUser user, String... applicationNames) {
        for (String appName : applicationNames) {
            Optional<AuthApplication> appOpt = applicationRepository.findByApplicationName(appName);
            if (appOpt.isPresent()) {
                AuthApplication application = appOpt.get();
                boolean alreadyAssigned = userApplicationRepository
                        .findByUserUserIdAndApplicationApplicationId(user.getUserId(), application.getApplicationId())
                        .isPresent();

                if (!alreadyAssigned) {
                    AuthUserApplication userApp = AuthUserApplication.builder()
                            .user(user)
                            .application(application)
                            .build();
                    userApplicationRepository.save(userApp);
                }
            }
        }
    }

    /**
     * 약관(Agreement) 기본 데이터 삽입 – 존재하지 않을 때만
     */
    private void initAgreements() {
        if (agreementRepository.count() > 0) return;

        ensureAgreementsForLanguage("ko", buildKoreanAgreements());
        ensureAgreementsForLanguage("en", buildEnglishAgreements());
    }

    /**
     * 지정된 언어 코드에 해당하는 약관이 없을 경우 기본 데이터를 저장한다.
     */
    private void ensureAgreementsForLanguage(String languageCode, List<Agreement> agreements) {
        if (agreementRepository.findByLanguageCode(languageCode).isEmpty()) {
            agreementRepository.saveAll(agreements);
        }
    }

    /**
     * 한국어 약관 기본 데이터 목록을 생성한다.
     */
    private List<Agreement> buildKoreanAgreements() {
        Agreement ag1 = Agreement.builder()
                .title("개인정보 수집·이용 동의")
                .content(String.join("\n",
                        "[필수] 개인정보 수집·이용 동의",
                        "",
                        "1. 수집·이용 목적",
                        "회원 식별 및 사내 시스템 접근 허가, 업무 기능 제공, 계약 이행, 업무 관련 공지 전달, 부정 이용 방지 및 비인가 사용 확인",
                        "",
                        "2. 수집 항목",
                        "① 사용자가 직접 입력하는 정보 : 이름, 이메일 주소, 비밀번호, 휴대전화번호",
                        "② 서비스 이용 과정에서 자동으로 수집되는 정보 : 접속 IP 주소, 서비스 이용 기록, 접속 로그",
                        "선택 정보 : 주소, 생년월일 등",
                        "",
                        "3. 보유 및 이용 기간",
                        "원칙) 회사 재직(또는 계약) 기간 동안 보유·이용하며, 퇴사·계약 종료 시 관련 규정에 따라 지체 없이 파기",
                        "예외) 전자상거래법 등 관련 법령에 따라 보존 의무가 있는 경우 해당 법령에서 정한 기간까지 보관 (예: 계약 및 청약철회 기록 5년)",
                        "",
                        "4. 동의 거부권 및 불이익 고지",
                        "귀하는 개인정보 수집·이용에 대한 동의를 거부할 권리가 있습니다. 단, 본 정보는 업무 시스템 이용에 필수적이므로, 동의를 거부하실 경우 서비스 이용이 제한될 수 있습니다."
                ))
                .languageCode("ko")
                .isRequired(true)
                .version("1.0")
                .effectiveDate(LocalDate.of(2024, 1, 1))
                .build();

        Agreement ag2 = Agreement.builder()
                .title("서비스 이용약관")
                .content(String.join("\n",
                        "[필수] 서비스 이용약관",
                        "",
                        "제1조 (목적) : 회사와 회원 간의 권리, 의무 및 책임사항 규정",
                        "",
                        "제2조 (용어의 정의) : '서비스', '회원', '게시물' 등",
                        "",
                        "제3조 (약관의 명시와 개정) : 서비스 홈페이지 게시, 개정 시 7일 전 공지",
                        "",
                        "제4조 (서비스의 제공 및 변경) : 제공 서비스 및 변경 가능성",
                        "",
                        "제5조 (회원의 의무) : 타인 정보 도용, 불법 정보 게시, 시스템 방해 행위 금지",
                        "",
                        "제6조 (회사의 의무) : 안정적 서비스 제공, 개인정보 보호",
                        "",
                        "제7조 (게시물의 저작권) : 게시물 저작권 귀속 및 이용 범위",
                        "",
                        "제8조 (회원 자격 및 계정 관리)",
                        "  1) 회원 자격은 회사와 유효한 재직 또는 계약 관계를 유지하는 동안에만 유효합니다.",
                        "  2) 회원은 임의로 서비스를 탈퇴할 수 없으며, 계정의 생성·변경·삭제는 시스템 관리자가 처리합니다.",
                        "  3) 퇴사 또는 계약 종료 시 회원의 서비스 이용 자격은 자동으로 상실되며 관리자는 내부 절차에 따라 계정을 비활성화 또는 삭제합니다.",
                        "",
                        "제9조 (손해배상 및 면책조항) : 책임 범위 및 면책",
                        "",
                        "제10조 (준거법 및 재판관할) : 대한민국 법률, 서울중앙지방법원"
                ))
                .languageCode("ko")
                .isRequired(true)
                .version("1.0")
                .effectiveDate(LocalDate.of(2024, 1, 1))
                .build();

        Agreement ag3 = Agreement.builder()
                .title("시스템 알림 수신 동의")
                .content(String.join("\n",
                        "[필수] 시스템 알림 수신 동의",
                        "",
                        "1. 수집·이용 목적",
                        "영수증 결재·반려, 시스템 점검, 보안 알림 등 업무용 시스템 알림 전송",
                        "",
                        "2. 수신 채널 (모두 적용)",
                        "이메일, 문자메시지(SMS/LMS/MMS), 앱 푸시 알림 — 모든 채널을 통해 시스템 알림이 발송됩니다.",
                        "",
                        "3. 보유 및 이용 기간",
                        "퇴사(계약 종료) 시 또는 계정 비활성화 시까지",
                        "",
                        "4. 동의 거부권 및 불이익 고지",
                        "본 동의는 업무 알림 제공을 위해 필수이며, 거부 시 서비스 이용이 제한됩니다.",
                        "",
                        "5. 철회 방법 안내",
                        "알림 수신 동의를 철회하려면 관리자를 통해 계정을 비활성화(퇴사) 해야 합니다."
                ))
                .languageCode("ko")
                .isRequired(true)
                .version("1.0")
                .effectiveDate(LocalDate.of(2024, 1, 1))
                .build();

        return List.of(ag1, ag2, ag3);
    }

    /**
     * 영어 약관 기본 데이터 목록을 생성한다.
    */
    private List<Agreement> buildEnglishAgreements() {
        Agreement ag1 = Agreement.builder()
                .title("Consent to Collect and Use Personal Information")
                .content(String.join("\n",
                        "[Required] Consent to Collect and Use Personal Information",
                        "",
                        "1. Purpose of Collection and Use",
                        "Identify members, grant access to in-house systems, provide work features, deliver work-related notices, prevent fraudulent use, and detect unauthorized access.",
                        "",
                        "2. Items Collected",
                        "① Information provided directly by the user: name, email address, password, mobile phone number",
                        "② Information automatically collected during service use: IP address, service usage records, access logs",
                        "Optional information: address, date of birth, etc.",
                        "",
                        "3. Retention and Use Period",
                        "In principle, retained and used during employment (or contract) period, and destroyed without delay upon termination in accordance with internal policies.",
                        "Exception: Retained as required by applicable laws (e.g., 5 years for contract and cancellation records under the Electronic Commerce Act).",
                        "",
                        "4. Right to Refuse and Possible Disadvantages",
                        "You may refuse to provide consent. However, the information is essential to use the work system, and refusal may restrict service access."
                ))
                .languageCode("en")
                .isRequired(true)
                .version("1.0")
                .effectiveDate(LocalDate.of(2024, 1, 1))
                .build();

        Agreement ag2 = Agreement.builder()
                .title("Service Terms of Use")
                .content(String.join("\n",
                        "[Required] Service Terms of Use",
                        "",
                        "Article 1 (Purpose): Defines the rights, obligations, and responsibilities between the company and members.",
                        "",
                        "Article 2 (Definitions): Defines 'services', 'members', 'posted content', etc.",
                        "",
                        "Article 3 (Presentation and Revision of Terms): Terms posted on the service website; revisions notified 7 days in advance.",
                        "",
                        "Article 4 (Provision and Change of Services): Provided services and possibility of change.",
                        "",
                        "Article 5 (Member Obligations): Prohibits identity theft, posting illegal information, and interfering with the system.",
                        "",
                        "Article 6 (Company Obligations): Provides stable service and protects personal information.",
                        "",
                        "Article 7 (Copyright of Posted Content): Ownership and usage scope of member-created content.",
                        "",
                        "Article 8 (Membership and Account Management):",
                        "  1) Membership is valid only while maintaining an active employment or contract with the company.",
                        "  2) Members cannot arbitrarily withdraw; system administrators manage account creation, change, and deletion.",
                        "  3) Membership automatically terminates upon resignation or contract expiration, and administrators deactivate or delete the account according to internal procedures.",
                        "",
                        "Article 9 (Damages and Disclaimer): Scope of liability and exemptions.",
                        "",
                        "Article 10 (Governing Law and Jurisdiction): Laws of the Republic of Korea and Seoul Central District Court."
                ))
                .languageCode("en")
                .isRequired(true)
                .version("1.0")
                .effectiveDate(LocalDate.of(2024, 1, 1))
                .build();

        Agreement ag3 = Agreement.builder()
                .title("Consent to Receive System Notifications")
                .content(String.join("\n",
                        "[Required] Consent to Receive System Notifications",
                        "",
                        "1. Purpose of Use",
                        "Send work-related system notifications such as receipt approval/rejection, maintenance notices, and security alerts.",
                        "",
                        "2. Delivery Channels (All Apply)",
                        "Email, SMS/LMS/MMS, and app push notifications — system notifications are delivered through all channels.",
                        "",
                        "3. Retention and Use Period",
                        "Until resignation (contract termination) or account deactivation.",
                        "",
                        "4. Right to Refuse and Possible Disadvantages",
                        "Consent is required to receive work notifications. Refusal may restrict access to services.",
                        "",
                        "5. How to Withdraw Consent",
                        "To withdraw consent, request account deactivation (resignation) through the administrator."
                ))
                .languageCode("en")
                .isRequired(true)
                .version("1.0")
                .effectiveDate(LocalDate.of(2024, 1, 1))
                .build();

        return List.of(ag1, ag2, ag3);
    }
}
