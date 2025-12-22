//package com.cs.hrm.config;
//
//import com.cs.hrm.entity.HrmEmploymentType;
//import com.cs.hrm.entity.HrmPositions;
//import com.cs.hrm.entity.HrmTeams;
//import com.cs.hrm.entity.HrmUser;
//import com.cs.hrm.repository.HrmEmploymentTypeRepository;
//import com.cs.hrm.repository.HrmPositionsRepository;
//import com.cs.hrm.repository.HrmTeamsRepository;
//import com.cs.hrm.repository.HrmUserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//
//import java.sql.Date;
//import java.time.LocalDate;
//import java.util.*;
//
///**
// * HRM Users 더미 데이터를 생성하여 Auth 쪽과 맞춰주는 예시
// * (더미 유저 '3,000' 생성 1분? 소요)
// * 
// * 더미 유저 : 1,000
// * 
// */
//@Configuration
//@RequiredArgsConstructor
//public class DataInitializerHrmUsers {
//
//    private final HrmUserRepository hrmUserRepository;
//    private final HrmTeamsRepository teamsRepository;
//    private final HrmPositionsRepository positionsRepository;
//    private final HrmEmploymentTypeRepository employmentTypeRepository;
//
//    @Bean
//    @Order(2)
//    CommandLineRunner initHrmUserData() {
//        return args -> {
//            // 1) 이미 HRM users 테이블에 데이터가 있으면 스킵
//            if (hrmUserRepository.count() > 0) {
//                return;
//            }
//
//            // 2) 팀 / 직급 / 고용형태 목록
//            List<HrmTeams> allTeams = teamsRepository.findAll();
//            List<HrmPositions> allPositions = positionsRepository.findAll();
//            List<HrmEmploymentType> allEmpTypes = employmentTypeRepository.findAll();
//            if (allTeams.isEmpty() || allPositions.isEmpty() || allEmpTypes.isEmpty()) {
//                System.out.println("⚠️ [DataInitializerHrmUsers] 부서/팀/직급/고용형태 중 하나가 비어있어서 HRM 유저 생성 스킵");
//                return;
//            }
//
//            // 3) 3,000명의 데이터 생성 & 100건씩 saveAll
//            final int TOTAL_COUNT = 1_000;
//            final int CHUNK_SIZE = 100;
//            Random random = new Random();
//            List<HrmUser> chunk = new ArrayList<>(CHUNK_SIZE);
//
//            for (int i = 1; i <= TOTAL_COUNT; i++) {
//                // 이메일 / 이름
//                String email = String.format("batchHrm%05d@gmail.com", i);
//                String name = "HRM유저_" + i;
//
//                // 팀/직급/고용형태 랜덤
//                HrmTeams randomTeam = allTeams.get(random.nextInt(allTeams.size()));
//                HrmPositions randomPosition = allPositions.get(random.nextInt(allPositions.size()));
//                HrmEmploymentType randomEmpType = allEmpTypes.get(random.nextInt(allEmpTypes.size()));
//
//                // 입사일
//                LocalDate joinDate = LocalDate.of(2022, 1, 1).plusDays(i % 365);
//
//                HrmUser hrmUser = HrmUser.builder()
//                        .email(email)
//                        .password("1234")
//                        .name(name)
//                        .birth("1990-01-01")
//                        .phoneNumber("010-0000-" + (1000 + i))
//                        .address("서울시 어딘가 " + i)
//                        .addressDetail("상세주소 " + i)
//                        .zipCode(10000 + i)
//                        .joiningDate(Date.valueOf(joinDate))
//                        .profileImgId(0)
//                        .position(randomPosition) // ManyToOne 직책
//                        .employmentTypeId(randomEmpType.getEmploymentTypeId()) // int 컬럼
//                        .team(randomTeam)        // ManyToOne 팀
//                        .build();
//
//                chunk.add(hrmUser);
//
//                // 🎯 100개 모이면 saveAll 후, 리스트 초기화
//                if (i % CHUNK_SIZE == 0) {
//                    hrmUserRepository.saveAll(chunk);
//                    chunk.clear();
//                }
//            }
//
//            // 나머지(100개 미만)가 있으면 처리
//            if (!chunk.isEmpty()) {
//                hrmUserRepository.saveAll(chunk);
//                chunk.clear();
//            }
//
//            System.out.println("✅ [DataInitializerHrmUsers] HRM 유저 1,000명 (100건씩) 더미 데이터 삽입 완료!");
//        };
//    }
//
//}


