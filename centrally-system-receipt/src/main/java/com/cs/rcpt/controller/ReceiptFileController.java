package com.cs.rcpt.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;
import org.springframework.security.core.Authentication;              // ★
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder; // ★

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.core.security.CustomUserPrincipal;               // ★ 여러분 프로젝트의 Principal
import com.cs.rcpt.entity.ReceiptAttachments;
import com.cs.rcpt.repository.ReceiptAttachmentsRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReceiptFileController {

    private final ReceiptAttachmentsRepository repo;

    @Value("${receipt.file.upload.path}")
    private String receiptPath;

    private Path basePath;

    @PostConstruct
    void init() { basePath = Paths.get(receiptPath); }

    private File resolve(String safeName) {
        return basePath.resolve(safeName).toFile();
    }

    // FIMXE : Properties??
    @GetMapping("/upload/receipt/{encodedSafeName:.+}")
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> download(
            @PathVariable("encodedSafeName") String encodedSafeName) throws Exception {

        /* 1) safeName 복원 */
        String safeName = UriUtils.decode(encodedSafeName, StandardCharsets.UTF_8);

        /* 2) DB 조회 (첨부 + 영수증 정보 확보) */
        ReceiptAttachments att = repo.findByFileOriginName(safeName)
            .orElseThrow(() -> new FileNotFoundException(GlobalExceptionHandler.CC + "첨부 없음: " + safeName));

        // FIXME : Role Properties??
        /* 3) ➡️ 접근 권한 확인 ------------------------------------ */
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserPrincipal principal = (CustomUserPrincipal) auth.getPrincipal();
        Integer userId      = principal.getUserId();
        boolean isAdminLike = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)   // "ROLE_…" 문자열
                .anyMatch(role ->
                    role.equals("ROLE_GATE_SYSTEM") ||
                    role.equals("ROLE_RECEIPT_APPROVER") ||
                    role.equals("ROLE_RECEIPT_MANAGER"));

        // 첨부 소유자이거나 관리자/회계 권한이 아니면 403
        if (!isAdminLike && !att.getReceipt().getUserId().equals(userId)) {
        	throw new IllegalArgumentException(GlobalExceptionHandler.CC + "영수증에 대한 접근 권한이 없습니다.");
        }
        /* ------------------------------------------------------ */

        /* 4) 디스크 파일 */
        File stored = resolve(safeName);
        if (!stored.exists()) throw new FileNotFoundException(GlobalExceptionHandler.CC + safeName);

        /* 5) Content-Disposition & MIME */
        boolean isImage = att.getFileType() != null &&
                          att.getFileType().toLowerCase().startsWith("image/");

        String disposition = (isImage ? "inline" : "attachment") +
                "; filename*=UTF-8''" + UriUtils.encode(att.getFileName(), StandardCharsets.UTF_8);

        MediaType media = isImage
                ? MediaType.parseMediaType(att.getFileType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .contentType(media)
                .body(new FileSystemResource(stored));
    }
}
