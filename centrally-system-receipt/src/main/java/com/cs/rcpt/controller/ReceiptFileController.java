package com.cs.rcpt.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.UriUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.core.security.CustomUserPrincipal;
import com.cs.core.service.DownloadQuotaService;
import com.cs.rcpt.entity.ReceiptAttachments;
import com.cs.rcpt.repository.ReceiptAttachmentsRepository;

import lombok.RequiredArgsConstructor;

/**
 * 영수증 첨부파일 접근 컨트롤러 (다운로드 및 미리보기)
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReceiptFileController {

    /** 캐시 유지 기간 (일) */
    private static final long CACHE_MAX_AGE_DAYS = 1L;

    private final ReceiptAttachmentsRepository repo;
    
    @Autowired
    private DownloadQuotaService downloadQuotaService;

    @Value("${receipt.file.upload.path}")
    private String receiptPath;

    private Path basePath;

    /**
     * 파일 업로드 경로 초기화
     */
    @PostConstruct
    void init() { basePath = Paths.get(receiptPath); }

    /**
     * 저장된 파일명으로 파일 경로 반환
     */
    private File resolve(String safeName) {
        return basePath.resolve(safeName).toFile();
    }

    /**
     * 영수증 첨부파일 접근 (게이트웨이를 거쳐 접근)
     * - 이미지 파일: 브라우저에서 미리보기 (inline)
     * - 일반 파일: 다운로드 (attachment)
     * - 원본 파일명으로 다운로드
     */
    @GetMapping("/upload/receipt/{encodedSafeName:.+}")
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> download(
            @PathVariable("encodedSafeName") String encodedSafeName,
            WebRequest webRequest) throws Exception {

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

        /* 5) 캐싱 처리 (Spring의 checkNotModified 사용) */
        long fileSize = stored.length();
        long lastModified = stored.lastModified();
        Instant lastModifiedInstant = Instant.ofEpochMilli(lastModified);
        String etag = "\"" + fileSize + "-" + lastModified + "\"";
        
        // Spring이 자동으로 304 처리 (ETag + Last-Modified 기반)
        // checkNotModified(String etag, long lastModified) 순서
        if (webRequest.checkNotModified(etag, lastModified)) {
            // 파일이 변경되지 않음 → 304 Not Modified 반환 (용량 제한 제외)
            return null; // Spring이 자동으로 304 응답 처리
        }

        /* 6) 다운로드 용량 제한 확인 (304 응답이 아닐 때만) */
        // 이미 Authentication을 가져왔으므로 재사용
        downloadQuotaService.checkAndIncrementDownloadSize(userId, fileSize);

        /* 7) Content-Disposition & MIME 설정 */
        // 이미지 파일 여부 확인
        boolean isImage = att.getFileType() != null &&
                          att.getFileType().toLowerCase().startsWith("image/");

        // 이미지면 "inline" (브라우저에서 미리보기), 일반 파일이면 "attachment" (다운로드)
        String originalFileName = att.getFileName() != null ? att.getFileName() : att.getFileOriginName();
        String encodedFileName = UriUtils.encode(originalFileName, StandardCharsets.UTF_8);
        String disposition = (isImage ? "inline" : "attachment") +
                "; filename=\"" + encodedFileName + "\"" +
                "; filename*=UTF-8''" + encodedFileName;

        // 이미지면 원본 MIME 타입 사용 (미리보기), 일반 파일이면 APPLICATION_OCTET_STREAM (다운로드)
        MediaType media = isImage
                ? MediaType.parseMediaType(att.getFileType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .lastModified(lastModifiedInstant)
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(CACHE_MAX_AGE_DAYS, TimeUnit.DAYS))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .contentType(media)
                .body(new FileSystemResource(stored));
    }
}
