//package com.cs.rcpt.config;
//
//import com.cs.rcpt.entity.*;
//import com.cs.rcpt.enums.ReceiptProcessStatus;          // ★ enum 직접 사용
//import com.cs.rcpt.repository.*;
//
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import lombok.Setter;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.math.BigDecimal;
//import java.sql.Date;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * Receipt + ApprovalLine 더미 데이터 생성
// *  ─ 신청자(userId=1)와 결재선 규칙은 기존과 동일
// */
//@Configuration
//@RequiredArgsConstructor
//public class DataInitializerReceipt {
//
//    /* ────── Repository 의존성 ────── */
//    private final ReceiptRepository        receiptRepository;
//    private final ReceiptCodeSeqRepository   seqRepo;
//    private final ReceiptCategoryRepository categoryRepository;
//
//    @Value("${receipt.file.upload.url}")
//    private String receiptFileUploadUrl;
//
//    /* ───────── 내부 클래스 & 샘플 사용자 ───────── */
//    @Getter @Setter @AllArgsConstructor
//    private static class UserInfo {
//        private Integer userId;
//        private String  name;
//        private String  department;
//        private String  team;
//    }
//
//    private static final Map<String,String> TEAM2DEPT = Map.ofEntries(
//        Map.entry("프레임워크팀", "SI사업본부"),
//        Map.entry("금융지원팀",   "SI사업본부"),
//        Map.entry("클라우드팀",   "SI사업본부"),
//        Map.entry("경영팀",      "경영관리본부"),
//        Map.entry("RPA팀",      "AI사업본부"),
//        Map.entry("챗봇팀",      "AI사업본부"),
//        Map.entry("인공지능팀",   "AI사업본부")
//    );
//
//    private Map<Integer,UserInfo> sampleUsers() {
//        record Seed(int id,String name,String team){
//            UserInfo toUser(){ return new UserInfo(id,name,TEAM2DEPT.get(team),team); }
//        }
//        List<Seed> list = List.of(
//            new Seed( 1,"관리자"  ,"프레임워크팀"),
//            new Seed( 2,"시스템"  ,"RPA팀"),
//            new Seed( 3,"카피바라","금융지원팀"),
//            new Seed( 4,"쿼카"    ,"클라우드팀"),
//            new Seed( 5,"햄스터"  ,"경영팀"),
//            new Seed( 6,"망나뇽"  ,"RPA팀"),
//            new Seed( 7,"너구리"  ,"프레임워크팀"),
//            new Seed( 8,"펭귄"    ,"금융지원팀"),
//            new Seed( 9,"햄스터"  ,"클라우드팀"),
//            new Seed(10,"판다"    ,"경영팀"),
//            new Seed(11,"토끼"    ,"RPA팀"),
//            new Seed(12,"여우"    ,"프레임워크팀"),
//            new Seed(13,"두더지"  ,"금융지원팀"),
//            new Seed(14,"사슴"    ,"클라우드팀")
//        );
//        return list.stream()
//                   .map(Seed::toUser)
//                   .collect(Collectors.toMap(UserInfo::getUserId, u -> u));
//    }
//
//    /* ───────── 초기화 루틴 ───────── */
//    @Bean
//    CommandLineRunner initReceiptData() {
//        return args -> {
//            if (receiptRepository.count() > 0) return;
//
//            /* 1️⃣ 카테고리 */
//            ReceiptCategory catNight = categoryRepository.save(
//                    new ReceiptCategory(1,"야근저녁",12_000,true));
//            ReceiptCategory catTeam  = categoryRepository.save(
//                    new ReceiptCategory(2,"회식비",20_000,true));
//
//            /* 2️⃣ 사용자 */
//            Map<Integer,UserInfo> users = sampleUsers();
//            UserInfo owner = users.get(1);            // 모든 영수증 신청자
//
//            /* 3️⃣ 더미 Receipt 생성 */
//            List<Receipt> receipts = new ArrayList<>();
//            for (int i = 1; i <= 13; i++) {
//
//                /* ── 상태 결정 ── */
//                ReceiptProcessStatus st = switch (i) {
//                    case 1,2,3   -> ReceiptProcessStatus.WAITING;
//                    case 4,5,6   -> ReceiptProcessStatus.REQUEST;
//                    case 7,8,9   -> ReceiptProcessStatus.REJECTED;
//                    case 10,11,12-> ReceiptProcessStatus.APPROVED;
//                    default      -> ReceiptProcessStatus.CLOSED;
//                };
//                
//                /* ▼ ① YYMM + 월별 시퀀스 생성 -------------------------- */
//                LocalDate subDate = LocalDate.now().minusDays(i % 2);            // 제출일
//                String    yymm    = subDate.format(DateTimeFormatter.ofPattern("yyMM"));
//    
//                ReceiptCodeSeq seq = seqRepo.findLock(yymm)                      // PESSIMISTIC LOCK
//                                            .orElseGet(() -> seqRepo.save(
//                                                  new ReceiptCodeSeq(){{
//                                                      setYymm  (yymm);
//                                                      setLastNo(0);
//                                                  }}));
//                int    nextNo = seq.next();      // 1,2,3…
//                seqRepo.save(seq);               // 업데이트 반영
//                String code   = yymm + "-" + nextNo;   // 예) 2506-7
//
//                /* ── Receipt 기본 필드 ── */
//                Receipt receipt = Receipt.builder()
//                		.receiptCode(code)
//                        .userId(owner.getUserId())
//                        .submissionDate(Date.valueOf(LocalDate.now().minusDays(i % 2)))
//                        .category((i % 2 == 0) ? catNight : catTeam)
//                        .reason("더미 데이터 " + i)
//                        .amount(BigDecimal.valueOf(10_000 + i * 100))
//                        .status(st)                                       // ★ enum 직접 저장
//                        .issuingLocation("서울시 위치 " + i)
//                        .attachment(ReceiptAttachments.builder()
//                                .fileUrl("%s/origin_sample%02d.png".formatted(receiptFileUploadUrl, i))
//                                .fileType("image/png")
//                                .fileOriginName("origin_sample%02d.png".formatted(i))
//                                .fileName("sample%02d.png".formatted(i))
//                                .uploadDate(Date.valueOf(LocalDate.now()))
//                                .build())
//                        .build();
//
//                /* ── 참여자 (1명) ── */
//                UserInfo part = users.get((i % 15 == 0) ? 15 : i % 15);
//                receipt.setParticipantsList(List.of(
//                        ReceiptParticipants.builder()
//                                .participantUserId(part.getUserId())
//                                .participantName(part.getName())
//                                .department(part.getDepartment())
//                                .team(part.getTeam())
//                                .receipt(receipt)
//                                .build()));
//
//                /* ── 결재선 패턴 ── */
//                // true = 승인, false = 대기 / 반려
//                boolean[] pattern = switch (st) {
//                    case WAITING  -> new boolean[]{ false, false, false, false, false };
//                    case REQUEST  -> new boolean[]{ true,  true,  false, false, false };
//                    case REJECTED -> new boolean[]{ true,  true,  false, false, false };
//                    case APPROVED, CLOSED
//                                 -> new boolean[]{ true,  true,  true,  true,  true  };
//                };
//                int rejectIdx = (st == ReceiptProcessStatus.REJECTED) ? 3 : -1;
//
//                List<String> sampleReasons = List.of(
//                        "영수증 사진과 금액 미일치",
//                        "영수증 내용 불명확",
//                        "팀 예산 한도 초과",
//                        "증빙 자료 부족",
//                        "복합 영수증으로 분리 필요");
//                Random rnd = new Random();
//                
//                // 반려 사유(첫 반려자)
//                String firstRejectReason = null;
//                LocalDateTime firstRejectDate = null;
//
//                List<ReceiptApprovalLine> lineList = new ArrayList<>();
//                int uid = 2;        // 결재 후보 순회
//                int approveCnt = 0;
//                for (int step = 1; step <= pattern.length; step++) {
//
//                    /* 신청자 제외 + 사용자 선택 */
//                    UserInfo apInfo;
//                    do {
//                        if (uid > 15) uid = 2;
//                        apInfo = users.get(uid++);
//                    } while (apInfo.getUserId().equals(owner.getUserId()));
//
//                    boolean sameDept = norm(apInfo.getDepartment())
//                                          .equals(norm(owner.getDepartment()));
//                    int role = sameDept ? 1 : (step == 2 ? 2 : 1);
//
//			        /* 결재 라인이 3개를 초과하면 자동으로 합의(2) 로 변경 */
//			        if (role == 1 && approveCnt >= 3) role = 2;
//			        if (role == 1) approveCnt++;
//
//                    boolean approved = pattern[step - 1];
//                    boolean isReject  = (!approved && step == rejectIdx);
//                    LocalDateTime now = LocalDateTime.now();
//                    
//                   // 첫 반려 정보 백업
//                   if (isReject && firstRejectReason == null) {
//                       firstRejectReason = sampleReasons.get(rnd.nextInt(sampleReasons.size()));
//                       firstRejectDate   = now;
//                   }
//                    
//                    lineList.add(ReceiptApprovalLine.builder()
//                            .stepOrder(step)
//                            .approvalRole(role)
//                            .approverUserId(apInfo.getUserId())
//                            .approverName(apInfo.getName())
//                            .department(apInfo.getDepartment())
//                            .team(apInfo.getTeam())
//                            .approvalStatus(approved)
//                            .approvedAt(approved ? now : null)
//                            .rejectedAt(isReject ? now : null)
//                            .rejectedReason(isReject ? sampleReasons.get(rnd.nextInt(sampleReasons.size())) : null)
//                            .receipt(receipt)
//                            .build());
//                }
//                receipt.setApprovalLines(lineList);
//                
//                if (st == ReceiptProcessStatus.REJECTED) {
//                    receipt.setCurrentApprovalStep(rejectIdx);
//                    receipt.setLastRejectionDate(Date.valueOf(firstRejectDate.toLocalDate()));
//                } else if (st == ReceiptProcessStatus.WAITING || st == ReceiptProcessStatus.REQUEST) {
//                    lineList.stream()
//                            .filter(l -> Boolean.FALSE.equals(l.getApprovalStatus()))
//                            .min(Comparator.comparingInt(ReceiptApprovalLine::getStepOrder))
//                            .ifPresent(p -> receipt.setCurrentApprovalStep(p.getStepOrder()));
//                }
//
//                receipts.add(receipt);
//            }
//
//            /* 저장 (Cascade) */
//            receiptRepository.saveAll(receipts);
//            System.out.println("✅ Dummy Receipt & ApprovalLine inserted successfully");
//        };
//    }
//
//    /* 공백·대소문자 무시용 헬퍼 */
//    private static String norm(String s) {
//        if (s == null) return "";
//        return s.replace('\u00A0',' ')
//                .replaceAll("\\s+","")
//                .toUpperCase(Locale.ROOT);
//    }
//}
