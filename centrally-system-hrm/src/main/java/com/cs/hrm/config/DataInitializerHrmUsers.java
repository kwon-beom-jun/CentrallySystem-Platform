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
     *   3) 원본 프로퍼티 문자열의 범위 부분을 선택된 숫자로 치환합니다.
     * '~'가 포함돼 있지 않으면 단일 파일 경로이므로 프로퍼티 값을 그대로 반환합니다.
     * 
     * 기본 프로필 이미지는 프론트엔드 정적 리소스(/img/profile/random/...)로 처리되므로 변환하지 않음
     */
    private String resolveRandomImgUrl() {
        String resolvedUrl = defaultProfileImg;
        
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
                    resolvedUrl = defaultProfileImg.replace(filePart, randomStr);
                }
            }
        }
        
        // 기본 프로필 이미지는 프론트엔드 정적 리소스로 처리 (/img/profile/random/... 그대로 유지)
        return resolvedUrl;
    }

    private HrmUserProfileImg getOrCreateDefaultImg() {
        String url = resolveRandomImgUrl();
        return profileImgRepository.findByFileUrl(url)
            .orElseGet(() -> {
                // 기본 프로필 이미지는 프론트엔드 정적 리소스(/img/profile/random/001.png)로 처리
                // fileName은 파일명만 저장 (예: random/001.png)
                String fileName = url.substring(url.lastIndexOf("/profile/") + "/profile/".length());
                
                HrmUserProfileImg def = HrmUserProfileImg.builder()
                        .fileUrl(url)  // /img/profile/random/001.png (프론트엔드 정적 리소스 경로)
                        .fileType("image/png")
                        .fileOriginName("default.png")
                        .fileName(fileName)  // random/001.png
                        .uploadDate(new java.sql.Date(System.currentTimeMillis()))
                        .build();
                return profileImgRepository.save(def);
            });
    }

    /**
     * 포트폴리오용: 데이터 초기화 기능 비활성화
     */
}

