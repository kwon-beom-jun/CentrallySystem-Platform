package com.cs.rcpt.service.impl;

import com.cs.core.service.EmailService;
import com.cs.rcpt.entity.Receipt;
import com.cs.rcpt.entity.ReceiptApprovalLine;
import com.cs.rcpt.service.ReceiptMailHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 영수증 메일 발송 헬퍼 서비스 구현체
 */
@Component
@RequiredArgsConstructor
public class ReceiptMailHelperImpl implements ReceiptMailHelper {

    private final EmailService emailService;

    /**
     * 다음 결재자에게 메일 발송
     */
    @Override
    public void notifyNextApprovers(Receipt receipt) {
        Integer nextStep = receipt.getCurrentApprovalStep();
        if (nextStep == null) return;

        List<String> emails = receipt.getApprovalLines().stream()
                .filter(l -> Objects.equals(l.getStepOrder(), nextStep))
                .map(ReceiptApprovalLine::getApproverEmail)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (emails.isEmpty()) return;

        String subject = "[영수증 결재 요청] 다음 단계 결재 필요 (" + receipt.getReceiptCode() + ")";
        String text = 
            "영수증 ID " + receipt.getReceiptCode() + " 에 대한 결재 요청이 도착했습니다.\n" +
            "영수증 제출자 : " + receipt.getUserName() + " (" + receipt.getUserEmail() + ")";

        emailService.send(emails.toArray(new String[0]), subject, text);
    }

    /**
     * 영수증 신청 시 첫 번째 결재자(현재 currentApprovalStep==1)에게 알림 메일 발송
     */
    @Override
    public void notifySubmissionFirstApprovers(Receipt receipt) {
        Integer step = 1;
        List<String> emails = receipt.getApprovalLines().stream()
                .filter(l -> Objects.equals(l.getStepOrder(), step))
                .map(ReceiptApprovalLine::getApproverEmail)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (emails.isEmpty()) return;

        String subject = "[Centrally-System] 영수증 결재 요청 - " + receipt.getReceiptCode();

        StringBuilder sb = new StringBuilder();
        sb.append("안녕하세요.\n\n")
          .append("새로운 영수증 결재 요청이 도착했습니다.\n\n")
          .append("[영수증 식별코드]   ").append(receipt.getReceiptCode()).append("\n")
          .append("[사유]                  ").append(Optional.ofNullable(receipt.getReason()).orElse("(미입력)")).append("\n")
          .append("[금액]                  ").append(Optional.ofNullable(receipt.getAmount()).map(a->a.toPlainString()).orElse("0")).append("원\n")
          .append("[제출자]                ").append(receipt.getUserName()).append(" (").append(receipt.getUserEmail()).append(")\n\n")
          .append("시스템에 접속하여 결재를 진행해 주세요.\n\n")
          .append("감사합니다.\nCENTRALLY-SYSTEM 운영팀");

        String text = sb.toString();

        emailService.send(emails.toArray(new String[0]), subject, text);
    }

    /**
     * 반려 시 관련자(기승인자 + 제출자)에게 메일 발송
     */
    @Override
    public void notifyReject(Receipt receipt, String reason) {

        /* 1) 기승인자 목록 ------------------------------------------------ */
        List<String> approverMails = receipt.getApprovalLines().stream()
                .filter(l -> l.getApprovedAt() != null)                       // 이미 승인
                .filter(l -> l.getApprovalRole() == null || l.getApprovalRole() != 2) // 합의(2) 제외
                // 대리 승인된 결재선은 대리자 이메일로 통보, 아니면 원결재자 이메일
                .map(l -> {
                    if (l.getDelegateMapping() != null && l.getDelegateMapping().getDelegateEmail() != null) {
                        return l.getDelegateMapping().getDelegateEmail();
                    }
                    return l.getApproverEmail();
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (!approverMails.isEmpty()) {
        	String subject = "[Centrally-System] 영수증 반려 통보 - " + receipt.getReceiptCode();
            String body    = buildRejectMailText(receipt, reason, false);    // 결재자용
            emailService.send(approverMails.toArray(new String[0]), subject, body);
        }

        /* 2) 제출자 ------------------------------------------------------- */
        String submitterMail = receipt.getUserEmail();
        if (submitterMail != null && !submitterMail.isBlank()) {
            String subject = "[Centrally-System] 제출하신 영수증이 반려되었습니다 - "
                           + receipt.getReceiptCode();
            String body    = buildRejectMailText(receipt, reason, true);     // 제출자용
            emailService.sendSimpleMessage(submitterMail, subject, body);
        }
    }

    /* 반려 메일 본문 생성 -------------------------------------------------- */
    private String buildRejectMailText(Receipt r, String reason, boolean forSubmitter) {
        StringBuilder sb = new StringBuilder();

        if (forSubmitter) {
            sb.append("안녕하세요, ").append(r.getUserName()).append("님.\n\n")
              .append("제출하신 아래 영수증이 반려되었습니다.\n\n");
        } else {
            sb.append("안녕하세요.\n\n")
              .append("참여하신 아래 영수증 결재 건이 반려되었습니다.\n\n");
        }

        sb.append("[영수증 식별코드]   ").append(r.getReceiptCode()).append("\n")
          .append("[반려 사유]              ").append(Optional.ofNullable(reason).orElse("(미입력)")).append("\n")
          .append("[제출자]                 ").append(r.getUserName())
          .append(" (").append(r.getUserEmail()).append(")\n\n")
          .append("확인 후 필요한 조치를 진행해 주세요.\n\n")
          .append("감사합니다.\nCENTRALLY-SYSTEM 운영팀");

        return sb.toString();
    }
}

