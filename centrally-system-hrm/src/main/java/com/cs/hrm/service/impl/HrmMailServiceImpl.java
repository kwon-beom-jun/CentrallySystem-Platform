package com.cs.hrm.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs.core.kafka.event.info.PostMailRequestEvent;
import com.cs.core.service.EmailService;
import com.cs.hrm.repository.HrmUserRepository;
import com.cs.hrm.service.HrmMailService;

import lombok.extern.slf4j.Slf4j;

/**
 * 메일 발송 전담 서비스 구현체
 * INFO 서비스의 게시글 메일 발송 요청 처리
 */
@Service
@Slf4j
public class HrmMailServiceImpl implements HrmMailService {

    @Autowired
    private HrmUserRepository userRepository;

    @Autowired(required = false)
    private EmailService emailService;

    /**
     * INFO 게시글 메일 발송 처리
     */
    @Override
    @Transactional(readOnly = true)
    public void sendInfoPostNotificationMail(PostMailRequestEvent event) {
        if (emailService == null) {
            log.warn("[HRM-MailService] EmailService is not configured");
            return;
        }

        // 역할 목록 생성
        List<String> roles = new ArrayList<>();
        if (Boolean.TRUE.equals(event.getMailIncludeGuest())) {
            roles.add("ROLE_INFO_GUEST");
        }
        if (Boolean.TRUE.equals(event.getMailIncludeUser())) {
            roles.add("ROLE_INFO_USER");
        }
        if (Boolean.TRUE.equals(event.getMailIncludeManager())) {
            roles.add("ROLE_INFO_MANAGER");
        }
        if (Boolean.TRUE.equals(event.getMailIncludeAdmin())) {
            roles.add("ROLE_INFO_ADMIN");
        }

        if (roles.isEmpty()) {
            log.warn("[HRM-MailService] No roles specified for mail");
            return;
        }

        // 이메일 조회
        List<String> emails;
        if (Boolean.TRUE.equals(event.getMailToAll())) {
            // 전체 발송 (역할 필터만)
//            log.info("[HRM-MailService] 전체 발송 모드 - 역할: {}", roles);
            emails = userRepository.findActiveEmailsByRoles(roles);
        } else if (event.getDepartmentIds() != null && !event.getDepartmentIds().isEmpty()) {
            // 부서 + 역할 필터
//            log.info("[HRM-MailService] 부서 필터 모드 - 역할: {}, 부서ID: {}", roles, event.getDepartmentIds());
            emails = userRepository.findActiveEmailsByRolesAndDepartments(
                    roles, event.getDepartmentIds());
        } else if (event.getTeamIds() != null && !event.getTeamIds().isEmpty()) {
            // 팀 + 역할 필터
//            log.info("[HRM-MailService] 팀 필터 모드 - 역할: {}, 팀ID: {}", roles, event.getTeamIds());
            emails = userRepository.findActiveEmailsByRolesAndTeams(
                    roles, event.getTeamIds());
        } else {
            log.warn("[HRM-MailService] No target specified (mailToAll=false, no depts/teams)");
            return;
        }

        log.info("[HRM-MailService] 조회된 이메일 수: {}", emails != null ? emails.size() : 0);
        if (emails != null && !emails.isEmpty()) {
            log.info("[HRM-MailService] 이메일 목록: {}", emails);
        }

        if (emails == null || emails.isEmpty()) {
            log.warn("[HRM-MailService] No emails found for the specified criteria");
            return;
        }

        // 메일 발송
        String subject = "[INFO 게시판] " + (event.getTitle() != null ? event.getTitle() : "");
        String text = stripHtmlTags(event.getContent() != null ? event.getContent() : "");

        emailService.send(emails.toArray(new String[0]), subject, text);
        
        log.info("[HRM-MailService] INFO 게시글 메일 발송 완료: {} 명", emails.size());
    }

    /**
     * HTML 태그 제거
     */
    private String stripHtmlTags(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }
        return html.replaceAll("<[^>]*>", "")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .trim();
    }
}

