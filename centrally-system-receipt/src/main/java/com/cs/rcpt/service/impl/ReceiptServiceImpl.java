package com.cs.rcpt.service.impl;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.core.util.FileNameUtils;
import com.cs.rcpt.controller.response.ReceiptPage;
import com.cs.rcpt.controller.response.ReceiptYearlySummaryResponse;
import com.cs.rcpt.entity.*;
import com.cs.rcpt.enums.ReceiptProcessStatus;
import com.cs.rcpt.repository.*;
import com.cs.rcpt.service.ReceiptService;
import com.cs.rcpt.utils.ReceiptConverter;
import com.cs.core.util.ImageProcessor;
import com.cs.core.util.ImageProcessor.ImageTooLargeException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 영수증 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    /* ────────── Repository 의존성 ────────── */
    private final ReceiptRepository         receiptRepository;
    private final ReceiptCodeSeqRepository 	seqRepo;
    private final ReceiptCategoryRepository categoryRepository;

    /* ────────── 외부 설정 값 ────────── */
    @Value("${file.upload.total.size}")
    private int  totalSize;              // 첨부파일 개수 한도(사용 시점은 선택)

    @Value("${receipt.file.upload.path}")
    private String receiptFileUploadPath;

    // 업로드 경로 사용 (게이트웨이를 거쳐 접근, 다운로드 및 미리보기)
    @Value("${receipt.file.upload.url}")
    private String receiptFileUploadUrl;

    /* --------------------------------------------------
     * 기본 조회 메서드
     * -------------------------------------------------- */

    /** (페이징) 전체 영수증 */
    @Override
    @Transactional
    public ReceiptPage<Receipt> getReceipts(int page, int size, Sort sort) {
        Pageable pageable = PageRequest.of(page, size, sort);
        return ReceiptConverter.toReceiptPage(receiptRepository.findAll(pageable));
    }

    /** (페이징) userId + statusCode 조건 조회 */
    @Override
    @Transactional
    public ReceiptPage<Receipt> getReceiptsByUserIdAndStatus(Integer userId,
    												ReceiptProcessStatus status,
                                                    int page, int size,
                                                    Sort sort) {
        Pageable pageable = PageRequest.of(page, size, sort);
        return ReceiptConverter.toReceiptPage(
                receiptRepository.findByUserIdAndStatus(userId, status, pageable));
    }
   
    /** (페이징) userId + 다중 statusCode 조건 조회 */
    @Override
    @Transactional
	public ReceiptPage<Receipt> getReceiptsByUserIdAndStatuses(Integer userId, List<ReceiptProcessStatus> statuses, int page, int size,
			Sort sort) {
		Pageable pageable = PageRequest.of(page, size, sort);

		Page<Receipt> pg = receiptRepository.findByUserIdAndStatusIn(userId, statuses, pageable);

		return ReceiptConverter.toReceiptPage(pg);
	}

    /** (페이징) userId 전부 조회 */
    @Override
    @Transactional
    public ReceiptPage<Receipt> getReceiptsByUserId(Integer userId,
                                           int page, int size,
                                           Sort sort) {
        Pageable pageable = PageRequest.of(page, size, sort);
        return ReceiptConverter.toReceiptPage(receiptRepository.findByUserId(userId, pageable));
    }

    /* --------------------------------------------------
     * 단건 / 생성
     * -------------------------------------------------- */
    @Override
    @Transactional
    public Receipt getReceipt(Integer id) {
        return receiptRepository.findById(id).orElse(null);
    }

    /** 사용자 영수증 생성 (multipart/form-data) */
    @Override
    @Transactional
    public Receipt createUserReceipt(Integer userId,
    								 String  userName,
    								 String  userEmail,
                                     String  dateStr,
                                     Integer categoryId,
                                     String  amountStr,
                                     String  reason,
                                     String  participantsJson,   // ← 이름 분리
                                     String  approversJson,
                                     Integer departmentId,
                                     String  departmentName,
                                     Integer teamId,
                                     String  teamName,
                                     MultipartFile file) {
        // 포트폴리오용: 기능 비활성화
        throw new RuntimeException("포트폴리오용: 영수증 생성 기능이 비활성화되어 있습니다.");
    }

    /* --------------------------------------------------
     * 기간/월별 조회 (기존 로직 유지 – 엔티티 구조 변경 無)
     * -------------------------------------------------- */
    @Override
    @Transactional
    public ReceiptPage<Receipt> getReceiptByUserIdAndYearMonth(Integer userId,
                                                      int page, int size,
                                                      String yearMonth,
                                                      Sort sort) {
        Pageable pageable = PageRequest.of(page, size, sort);

        if (yearMonth == null || yearMonth.isBlank())
            return ReceiptConverter.toReceiptPage(receiptRepository.findByUserId(userId, pageable));

        String[] ym = yearMonth.split("-");
        if (ym.length != 2)                          // 형식 오류 시 전체 조회
            return ReceiptConverter.toReceiptPage(receiptRepository.findByUserId(userId, pageable));

        LocalDate start = LocalDate.of(Integer.parseInt(ym[0]), Integer.parseInt(ym[1]), 1);
        LocalDate end   = start.plusMonths(1).minusDays(1);

        return ReceiptConverter.toReceiptPage(
            receiptRepository.findByUserIdAndSubmissionDateBetween(
                userId, Date.valueOf(start), Date.valueOf(end), pageable));
    }

    @Override
    @Transactional
    public ReceiptPage<Receipt> getReceiptsBySearchUserDateRange(Integer userId,
                                                        String startDateStr,
                                                        String endDateStr,
                                                        ReceiptProcessStatus status,
                                                        int page, int size,
                                                        Sort sort) {
        Pageable pageable = PageRequest.of(page, size, sort);

        Date startDate = Date.valueOf(
                (startDateStr == null || startDateStr.isBlank()) ?
                        LocalDate.now() : LocalDate.parse(startDateStr));
        Date endDate   = Date.valueOf(
                (endDateStr == null || endDateStr.isBlank()) ?
                        LocalDate.now() : LocalDate.parse(endDateStr));

        return ReceiptConverter.toReceiptPage(
            receiptRepository.findReceiptDynamic(userId, startDate, endDate, status, pageable));
    }

    @Override
    @Transactional
    public ReceiptPage<Receipt> getReceiptsByYearMonthForAll(int page, int size,
                                                    String yearMonth,
                                                    Sort sort) {
        Pageable pageable = PageRequest.of(page, size, sort);

        if (yearMonth == null || yearMonth.isBlank())
            return ReceiptConverter.toReceiptPage(receiptRepository.findAll(pageable));

        String[] ym = yearMonth.split("-");
        if (ym.length != 2)
            return ReceiptConverter.toReceiptPage(receiptRepository.findAll(pageable));

        LocalDate start = LocalDate.of(Integer.parseInt(ym[0]), Integer.parseInt(ym[1]), 1);
        LocalDate end   = start.plusMonths(1).minusDays(1);

        return ReceiptConverter.toReceiptPage(
            receiptRepository.findBySubmissionDateBetween(
                Date.valueOf(start), Date.valueOf(end), pageable));
    }
    
    /* --------------------------------------------------
     * 연도별 요약 (기존 로직 유지)
     * -------------------------------------------------- */
    @Override
    @Transactional
    public ReceiptYearlySummaryResponse getReceiptByUserYearlySummary(Integer userId, int year) {

        List<Object[]> raw = receiptRepository.findYearlySummaryByUserId(userId, year);
        /* 1) 1~12월 기본값 0 세팅 */
        List<ReceiptYearlySummaryResponse.MonthlySummary> monthly = IntStream.rangeClosed(1, 12)
                .mapToObj(m -> ReceiptYearlySummaryResponse.MonthlySummary.builder()
                        .month(m).waiting(0).request(0).approved(0)
                        .rejected(0).closed(0).sum(0).build())
                .collect(Collectors.toList());

        /* 2) 집계 */
        for (Object[] r : raw) {
            int  mo = ((Number) r[0]).intValue();   // 월
            int  sc = ((Number) r[1]).intValue();   // 상태코드
            long am = ((Number) r[2]).longValue();  // 금액
            var ms  = monthly.get(mo - 1);          // 월별 DTO

            switch (sc) {
                case 4  -> ms.setWaiting(ms.getWaiting() + am);
                case 1  -> ms.setRequest(ms.getRequest() + am);
                case 2  -> ms.setApproved(ms.getApproved() + am);
                case 3  -> ms.setRejected(ms.getRejected() + am);
                case 5  -> ms.setClosed(ms.getClosed() + am);
            }
            ms.setSum(ms.getSum() + am);
        }

        /* 3) 연간 합계 계산 */
        long totWait = monthly.stream().mapToLong(ReceiptYearlySummaryResponse.MonthlySummary::getWaiting).sum();
        long totReq  = monthly.stream().mapToLong(ReceiptYearlySummaryResponse.MonthlySummary::getRequest).sum();
        long totApr  = monthly.stream().mapToLong(ReceiptYearlySummaryResponse.MonthlySummary::getApproved).sum();
        long totRej  = monthly.stream().mapToLong(ReceiptYearlySummaryResponse.MonthlySummary::getRejected).sum();
        long totCls  = monthly.stream().mapToLong(ReceiptYearlySummaryResponse.MonthlySummary::getClosed).sum();
        long totSum  = monthly.stream().mapToLong(ReceiptYearlySummaryResponse.MonthlySummary::getSum).sum();

        return ReceiptYearlySummaryResponse.builder()
                .year(year).monthlyList(monthly)
                .totalWaiting(totWait).totalRequest(totReq)
                .totalApproved(totApr).totalRejected(totRej)
                .totalClosed(totCls).totalSum(totSum)
                .build();
    }


    /* --------------------------------------------------
     * 영수증 수정
     * -------------------------------------------------- */
    @Override
    @Transactional
    public Receipt updateReceipt(Integer id, Receipt updated) {
        return receiptRepository.findById(id)
                .map(orig -> {
                    orig.setUserId(updated.getUserId());
                    orig.setSubmissionDate(updated.getSubmissionDate());
                    orig.setCategory(updated.getCategory());
                    orig.setReason(updated.getReason());
                    orig.setAmount(updated.getAmount());
                    orig.setIssuingLocation(updated.getIssuingLocation());
                    orig.setStatus(updated.getStatus());
                    orig.setAttachment(updated.getAttachment());
                    return receiptRepository.save(orig);
                })
                .orElseGet(() -> receiptRepository.save(updated));
    }

    
    /**
     * 영수증 부분 수정(patch)  
     *  - 전달된 값만 반영하고, 전달되지 않은 필드는 그대로 둔다.
     *  - 결재선(approvers)·참석자(participants)·첨부파일까지 모두 처리한다.
     */
    @Override
    @Transactional
    public Receipt patchReceipt(Integer userId, Integer receiptId,
                                String dateStr, Integer categoryId, String amountStr, String reason,
                                String participantsJson, String approversJson,
                                MultipartFile newFile, Boolean deleteFile) {

        /* 1) 원본 영수증 조회 ──────────────────────────────────────────────── */
        Receipt r = receiptRepository.findById(receiptId)
            .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "Receipt not found"));

        /* 2) 소유자 권한 체크 */
        if (!Objects.equals(r.getUserId(), userId))
            throw new RuntimeException(GlobalExceptionHandler.CC + "다른 사용자의 영수증입니다");

        /* 3) 단순 필드 갱신 (널이면 무시) */
        if (dateStr    != null) r.setSubmissionDate(Date.valueOf(dateStr));
        if (categoryId != null) {
            ReceiptCategory cat = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "카테고리 없음"));
            r.setCategory(cat);
        }
        if (amountStr  != null) r.setAmount(new BigDecimal(amountStr));
        if (reason     != null) r.setReason(reason);

        /* 4) 참가자(participants) 업데이트 ----------------------------------- */
        if (participantsJson != null) {
            /* 4-1) 프런트 JSON → 새 참가자 목록 */
            List<ReceiptParticipants> newPts =
                ReceiptConverter.parseParticipants(participantsJson, r);   // userId, name, 부서, 팀 포함

            /* 4-2) 현재 참가자 Map<key, entity> (활성·비활성 모두)
             *  - INTERNAL: key = "INTERNAL:"+userId
             *  - EXTERNAL: key = "EXTERNAL:"+name+"|"+phone (간단 중복 방지)
             */
            Map<String, ReceiptParticipants> current =
                r.getParticipantsList().stream()
                  .collect(Collectors.toMap(
                      p -> {
                          String t = p.getParticipantType();
                          if ("EXTERNAL".equalsIgnoreCase(t)) {
                              String name = Optional.ofNullable(p.getParticipantName()).orElse("");
                              String phone = Optional.ofNullable(p.getPhone()).orElse("");
                              return "EXTERNAL:" + name + "|" + phone;
                          } else {
                              return "INTERNAL:" + String.valueOf(p.getParticipantUserId());
                          }
                      },
                      p -> p));

            /* 4-3) 신규 목록 순회하여 ADD / UPDATE */
            for (ReceiptParticipants np : newPts) {
                String key;
                if ("EXTERNAL".equalsIgnoreCase(np.getParticipantType())) {
                    String name = Optional.ofNullable(np.getParticipantName()).orElse("");
                    String phone = Optional.ofNullable(np.getPhone()).orElse("");
                    key = "EXTERNAL:" + name + "|" + phone;
                } else {
                    key = "INTERNAL:" + String.valueOf(np.getParticipantUserId());
                }
                ReceiptParticipants op = current.remove(key);

                if (op == null) {                // (1) 완전 신규 → 추가
                    r.getParticipantsList().add(np);

                } else {                         // (2) 기존 존재 → 필드만 갱신
                    op.setParticipantType(np.getParticipantType());
                    op.setParticipantName(np.getParticipantName());
                    op.setCompany        (np.getCompany());
                    op.setPosition       (np.getPosition());
                    op.setPhone          (np.getPhone());
                    op.setDepartment     (np.getDepartment());
                    op.setTeam           (np.getTeam());
                    /* 비활성 상태였다면 재활성화 */
                    op.setEnabled(true);
                    op.setDeletedAt(null);
                }
            }

            /* 4-4) current 에 남은 행 = 프런트에서 제거됨 → soft-delete */
            for (ReceiptParticipants orphan : current.values()) {
                orphan.setEnabled(false);
                orphan.setDeletedAt(LocalDateTime.now());
            }
        }

        /* 5) 결재선(approvers) 업데이트 ------------------------------------ */
        if (approversJson != null) {
            /* 5-1) 프런트 JSON → 새 결재선 목록 */
            List<ReceiptApprovalLine> newLines = parseApprovers(approversJson, r);

            /* 5-2) 현재 결재선 Map<userId, line> (활성/비활성 구분 없이 모두) */
            Map<Integer, ReceiptApprovalLine> current =
                r.getApprovalLines().stream()
                  .collect(Collectors.toMap(ReceiptApprovalLine::getApproverUserId, l -> l));

            /* 5-3) 신규 목록 순회하여 ADD / UPDATE */
            for (ReceiptApprovalLine nl : newLines) {
                ReceiptApprovalLine old = current.remove(nl.getApproverUserId());

                if (old == null) {                           // (1) 완전 신규
                    r.getApprovalLines().add(nl);
                } else {                                     // (2) 기존 존재 → 필드만 업데이트
                    old.setApprovalRole(nl.getApprovalRole());
                    old.setDepartment  (nl.getDepartment());
                    old.setTeam        (nl.getTeam());
                    old.setApproverEmail(nl.getApproverEmail());
                    old.setStepOrder   (nl.getStepOrder());
                    /* 승인 여부·일자는 유지 (필요 시 추가 업데이트) */
                }
            }

            /* 5-4) current 에 남은 라인 = 프런트에서 제거됨 ⇒ soft-delete */
            for (ReceiptApprovalLine orphan : current.values()) {
                orphan.setEnabled(false);
                orphan.setDeletedAt(LocalDateTime.now());
            }

            /* 5-5) currentApprovalStep 재계산 (미승인 + 활성인 라인 중 최소 step) */
            r.setCurrentApprovalStep(
                r.getApprovalLines().stream()
                 .filter(l -> !Boolean.TRUE.equals(l.getApprovalStatus()) && Boolean.TRUE.equals(l.getEnabled()))
                 .map(ReceiptApprovalLine::getStepOrder)
                 .min(Integer::compareTo)
                 .orElse(null));

            /* 5-6) '반려' 상태에서 결재선을 수정한 경우 ────────────
             *      ↳ 영수증 status = REJECTED 는 유지하고,
             *        모든 결재라인을 '대기' 상태로만 초기화                */
            if (r.getStatus() == ReceiptProcessStatus.REJECTED) {

                /* 결재라인 초기화: 승인·반려 정보 제거 → 대기로 전환 */
                for (ReceiptApprovalLine line : r.getApprovalLines()) {
                    line.setApprovalStatus(false);   // 승인 → 대기
                    line.setApprovedAt(null);        // 승인 일시 제거
                    line.setRejectedAt(null);        // 반려 일시 제거
//                    line.setRejectedReason(null);    // 반려 사유 제거
                }

                /* 대기 중인(미승인·활성) 라인 중 가장 앞 순서를 currentApprovalStep 으로 */
                r.setCurrentApprovalStep(
                    r.getApprovalLines().stream()
                      .filter(l -> !Boolean.TRUE.equals(l.getApprovalStatus())
                                && Boolean.TRUE.equals(l.getEnabled()))
                      .map(ReceiptApprovalLine::getStepOrder)
                      .min(Integer::compareTo)
                      .orElse(null));
            }
        }

        /* 6) 첨부파일 처리 ----------------------------------------------- */
        if (Boolean.TRUE.equals(deleteFile)) {           // 삭제 요청
            r.setAttachment(null);
        } else if (newFile != null && !newFile.isEmpty()) { // 새 파일 업로드
            r.setAttachment(storeAttachment(newFile, r));
        }

        /* 7) 저장(Dirty Checking) ---------------------------------------- */
        return receiptRepository.save(r);
    }


    /* --------------------------------------------------
     * 삭제
     * -------------------------------------------------- */
    @Override
    @Transactional
    public void deleteReceipt(Integer id) {
        receiptRepository.deleteById(id);
    }

    /** 첨부파일 저장 후 엔티티 반환 */
    private ReceiptAttachments storeAttachment(MultipartFile file, Receipt owner) {
        try {
            String origin = file.getOriginalFilename();
            String safe   = FileNameUtils.safeName(origin);
            File destDir  = new File(receiptFileUploadPath);
            try {
                ImageProcessor.processAndSave(
                    file,
                    destDir,
                    safe,
                    2,
                    2048
                );
            } catch (ImageTooLargeException ex) {
                throw new RuntimeException(GlobalExceptionHandler.CC + "압축 진행 후 이미지 용량 초과(" + origin + ")", ex);
            }

            // File 경로는 ImageProcessor 내부에서 처리되므로 별도 File 참조 불필요

            // URL 구성 (프로퍼티는 이미 상대 경로)
            String fileUrl = receiptFileUploadUrl + "/" + FileNameUtils.encodePathSegment(safe);

            return ReceiptAttachments.builder()
                    .fileUrl(fileUrl)
                    .fileType(file.getContentType())
                    .uploadDate(Date.valueOf(LocalDate.now()))
                    .fileOriginName(safe)
                    .fileName(origin)
                    .receipt(owner)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(GlobalExceptionHandler.CC + "파일 저장 실패", e);
        }
    }
    
    /** approvers JSON → ReceiptApprovalLine 목록 */
    private List<ReceiptApprovalLine> parseApprovers(String json, Receipt owner) {
        try {
            List<Map<String, Object>> src =
                new ObjectMapper().readValue(json, new TypeReference<>() {});

            AtomicInteger order = new AtomicInteger(1);

            return src.stream().map(m -> {
                /* ─ 역할 코드 계산 ─────────────────────── */
				/* ① 프런트가 넘겨준 role 이 있으면 그대로 사용 */
				Integer roleObj = (Integer) m.get("approvalRole");
				int role;
				if (roleObj != null) {
				    role = roleObj;
				} else {
				    /* ② 없을 때만 계산 */
				    boolean isDefault = Boolean.TRUE.equals(m.get("isDefault"));
				    if (isDefault)                          role = 3;
				    else if ("합의".equals(m.get("approvalType"))) role = 2;
				    else                                         role = 1;
				}

                return ReceiptApprovalLine.builder()
                        .stepOrder     (order.getAndIncrement())
                        .approvalRole  (role)
                        .approverUserId((Integer) m.get("userId"))
                        .approverName  ((String)  m.get("name"))
                        .approverEmail ((String)  m.get("email"))
                        .department    ((String)  m.get("department"))
                        .team          ((String)  m.get("team"))
                        .approvalStatus(false)
                        .receipt(owner)
                        .build();
            }).toList();

        } catch (Exception e) {
            throw new RuntimeException(
                GlobalExceptionHandler.CC + "approvers JSON 파싱 실패", e);
        }
    }
}

