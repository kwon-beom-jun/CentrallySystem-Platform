package com.cs.hrm.config;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cs.hrm.entity.HrmEmploymentType;
import com.cs.hrm.enums.EmploymentType;
import com.cs.hrm.entity.HrmPositions;
import com.cs.hrm.entity.HrmTeams;
import com.cs.hrm.entity.HrmUser;
import com.cs.hrm.entity.HrmUserProfileImg;
import com.cs.hrm.repository.HrmEmploymentTypeRepository;
import com.cs.hrm.repository.HrmPositionsRepository;
import com.cs.hrm.repository.HrmTeamsRepository;
import com.cs.hrm.repository.HrmUserProfileImgRepository;
import com.cs.hrm.repository.HrmUserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * HRM Users 더미 데이터를 생성하여 Auth 쪽과 맞춰주는 예시
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializerHrmUsers {

    private final HrmUserRepository hrmUserRepository;
    private final HrmTeamsRepository teamsRepository;
    private final HrmPositionsRepository positionsRepository;
    private final HrmEmploymentTypeRepository employmentTypeRepository;
    private final HrmUserProfileImgRepository  profileImgRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${hrm.default.profile.img}")
    private String defaultProfileImg;

    /**
     * hrm.default.profile.img 프로퍼티가
     *   /img/profile/random/001~009.png 형태처럼 '~' 기호로 범위를 지정할 경우,
     *   1) 파일명 부분(001~009)을 추출하여 시작·끝 값을 구한 뒤
     *   2) 그 사이 숫자 중 하나를 무작위(Random)로 선택하고, 자릿수(0 패딩)를 유지해 문자열로 변환합니다.
     *   3) 원본 프로퍼티 문자열의 범위 부분을 선택된 숫자로 치환해 최종 URL을 반환합니다.
     * '~'가 포함돼 있지 않으면 단일 파일 경로이므로 프로퍼티 값을 그대로 돌려줍니다.
     */
    private String resolveRandomImgUrl() {
        if (defaultProfileImg.contains("~")) {
            int lastSlash = defaultProfileImg.lastIndexOf('/');
            int dotIdx = defaultProfileImg.lastIndexOf('.');
            if (lastSlash != -1 && dotIdx != -1) {
                String filePart = defaultProfileImg.substring(lastSlash + 1, dotIdx); // e.g. 001~009
                String[] parts = filePart.split("~");
                if (parts.length == 2) {
                    int start = Integer.parseInt(parts[0]);
                    int end = Integer.parseInt(parts[1]);
                    int random = new java.util.Random().nextInt(end - start + 1) + start;
                    String randomStr = String.format("%0" + parts[0].length() + "d", random);
                    return defaultProfileImg.replace(filePart, randomStr);
                }
            }
        }
        return defaultProfileImg;
    }

    private HrmUserProfileImg getOrCreateDefaultImg() {
        String url = resolveRandomImgUrl();
        return profileImgRepository.findByFileUrl(url)
            .orElseGet(() -> {
                HrmUserProfileImg def = HrmUserProfileImg.builder()
                        .fileUrl(url)
                        .fileType("image/png")
                        .fileOriginName("default.png")
                        .fileName("default.png")
                        .uploadDate(new java.sql.Date(System.currentTimeMillis()))
                        .build();
                return profileImgRepository.save(def);
            });
    }

    // ============================================
    // 더미데이터 초기화 로직 제거됨 (이력용 포트폴리오)
    // ============================================
    // @Bean
    // @Order(2)
    // CommandLineRunner initHrmUserData() {
    //     return args -> {
    //         // 더미데이터 생성 로직이 제거되었습니다.
    //     };
    // }
}
