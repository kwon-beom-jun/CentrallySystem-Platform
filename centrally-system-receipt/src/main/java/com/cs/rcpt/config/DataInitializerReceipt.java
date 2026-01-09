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

    private static final Map<String,String> TEAM2DEPT = Map.ofEntries(
        Map.entry("프레임워크팀", "SI사업본부"),
        Map.entry("금융지원팀",   "SI사업본부"),
        Map.entry("클라우드팀",   "SI사업본부"),
        Map.entry("경영팀",      "경영관리본부"),
        Map.entry("RPA팀",      "AI사업본부"),
        Map.entry("챗봇팀",      "AI사업본부"),
        Map.entry("인공지능팀",   "AI사업본부")
    );

    private Map<Integer,UserInfo> sampleUsers() {
        record Seed(int id,String name,String email,String team){
            UserInfo toUser(){ return new UserInfo(id,name,email,TEAM2DEPT.get(team),team); }
        }
        List<Seed> list = List.of(
            new Seed( 1,"관리자"  ,"test","프레임워크팀"),
            new Seed( 2,"시스템"  ,"system","RPA팀"),
            new Seed( 3,"카피바라","user1@gmail.com","금융지원팀"),
            new Seed( 4,"쿼카"    ,"user2@gmail.com","클라우드팀"),
            new Seed( 5,"햄스터"  ,"user3@gmail.com","경영팀"),
            new Seed( 6,"망나뇽"  ,"user4@gmail.com","RPA팀"),
            new Seed( 7,"너구리"  ,"user5@gmail.com","프레임워크팀"),
            new Seed( 8,"펭귄"    ,"user6@gmail.com","금융지원팀"),
            new Seed( 9,"햄스터"  ,"user7@gmail.com","클라우드팀"),
            new Seed(10,"판다"    ,"user8@gmail.com","경영팀"),
            new Seed(11,"토끼"    ,"user9@gmail.com","RPA팀"),
            new Seed(12,"여우"    ,"user10@gmail.com","프레임워크팀"),
            new Seed(13,"두더지"  ,"user11@gmail.com","금융지원팀"),
            new Seed(14,"사슴"    ,"user12@gmail.com","클라우드팀")
        );
        return list.stream()
                   .map(Seed::toUser)
                   .collect(Collectors.toMap(UserInfo::getUserId, u -> u));
    }

    /**
     * 포트폴리오용: 데이터 초기화 기능 비활성화
     */

    /* 공백·대소문자 무시용 헬퍼 */
    private static String norm(String s) {
        if (s == null) return "";
        return s.replace('\u00A0',' ')
                .replaceAll("\\s+","")
                .toUpperCase(Locale.ROOT);
    }
}
