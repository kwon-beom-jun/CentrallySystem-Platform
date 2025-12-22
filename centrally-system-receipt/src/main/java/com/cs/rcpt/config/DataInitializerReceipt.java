package com.cs.rcpt.config;

import com.cs.rcpt.entity.*;
import com.cs.rcpt.enums.ReceiptProcessStatus;          // ★ enum 직접 사용
import com.cs.rcpt.repository.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Receipt + ApprovalLine 더미 데이터 생성
 *  ─ 신청자(userId=1)와 결재선 규칙은 기존과 동일
 */
@Configuration
@RequiredArgsConstructor
public class DataInitializerReceipt {

    /* ────── Repository 의존성 ────── */
    private final ReceiptRepository        receiptRepository;
    private final ReceiptCodeSeqRepository   seqRepo;
    private final ReceiptCategoryRepository categoryRepository;

    @Value("${receipt.file.upload.url}")
    private String receiptFileUploadUrl;

    /* ───────── 내부 클래스 & 샘플 사용자 ───────── */
    @Getter @Setter @AllArgsConstructor
    private static class UserInfo {
        private Integer userId;
        private String  name;
        private String  email;
        private String  department;
        private String  team;
    }

    // ============================================
    // 더미데이터 초기화 로직 제거됨 (이력용 포트폴리오)
    // 실제 부서/팀 정보는 더미 값으로 대체됨
    // ============================================
    private static final Map<String,String> TEAM2DEPT = Map.ofEntries(
        Map.entry("Dummy Team 1", "Dummy Department 1"),
        Map.entry("Dummy Team 2", "Dummy Department 1"),
        Map.entry("Dummy Team 3", "Dummy Department 1"),
        Map.entry("Dummy Team 4", "Dummy Department 2"),
        Map.entry("Dummy Team 5", "Dummy Department 3"),
        Map.entry("Dummy Team 6", "Dummy Department 3"),
        Map.entry("Dummy Team 7", "Dummy Department 3")
    );

    private Map<Integer,UserInfo> sampleUsers() {
        record Seed(int id,String name,String email,String team){
            UserInfo toUser(){ return new UserInfo(id,name,email,TEAM2DEPT.get(team),team); }
        }
        // ============================================
        // 더미데이터 초기화 로직 제거됨 (이력용 포트폴리오)
        // 실제 사용자 정보는 더미 값으로 대체됨
        // ============================================
        List<Seed> list = List.of(
            new Seed( 1,"Dummy Admin"  ,"test","Dummy Team 1"),
            new Seed( 2,"Dummy System"  ,"system","Dummy Team 5"),
            new Seed( 3,"Dummy User 1","user1@example.com","Dummy Team 2"),
            new Seed( 4,"Dummy User 2"    ,"user2@example.com","Dummy Team 3"),
            new Seed( 5,"Dummy User 3"  ,"user3@example.com","Dummy Team 4"),
            new Seed( 6,"Dummy User 4"  ,"user4@example.com","Dummy Team 5"),
            new Seed( 7,"Dummy User 5"  ,"user5@example.com","Dummy Team 1"),
            new Seed( 8,"Dummy User 6"    ,"user6@example.com","Dummy Team 2"),
            new Seed( 9,"Dummy User 7"  ,"user7@example.com","Dummy Team 3"),
            new Seed(10,"Dummy User 8"    ,"user8@example.com","Dummy Team 4"),
            new Seed(11,"Dummy User 9"    ,"user9@example.com","Dummy Team 5"),
            new Seed(12,"Dummy User 10"    ,"user10@example.com","Dummy Team 1"),
            new Seed(13,"Dummy User 11"  ,"user11@example.com","Dummy Team 2"),
            new Seed(14,"Dummy User 12"    ,"user12@example.com","Dummy Team 3")
        );
        return list.stream()
                   .map(Seed::toUser)
                   .collect(Collectors.toMap(UserInfo::getUserId, u -> u));
    }

    /* ───────── 초기화 루틴 ───────── */
    // ============================================
    // 더미데이터 초기화 로직 제거됨 (이력용 포트폴리오)
    // ============================================
    // @Bean
    // CommandLineRunner initReceiptData(TransactionTemplate tx) {
    //     return args -> tx.execute(status -> {
    //         seedData();
    //         return null;
    //     });
    // }
    // void seedData() {
    //     if (receiptRepository.count() > 0) return;
    //     // 더미데이터 생성 로직이 제거되었습니다.
    // }

    /* 공백·대소문자 무시용 헬퍼 */
    private static String norm(String s) {
        if (s == null) return "";
        return s.replace('\u00A0',' ')
                .replaceAll("\\s+","")
                .toUpperCase(Locale.ROOT);
    }
}
