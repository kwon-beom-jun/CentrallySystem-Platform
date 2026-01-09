package com.cs.info.controller;

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

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.core.security.CustomUserPrincipal;
import com.cs.core.service.DownloadQuotaService;
import com.cs.info.entity.InfoBoardDefinition;
import com.cs.info.entity.InfoPostAttachment;
import com.cs.info.enums.BoardCode;
import com.cs.info.repository.InfoBoardDefinitionRepository;
import com.cs.info.repository.InfoPostAttachmentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.RequiredArgsConstructor;

/**
 * 게시글 첨부파일 접근 컨트롤러 (다운로드 및 미리보기)
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class InfoPostFileController {

    /** 캐시 유지 기간 (일) */
    private static final long CACHE_MAX_AGE_DAYS = 1L;

    private final InfoPostAttachmentRepository attachmentRepository;
    
    @Autowired
    private InfoBoardDefinitionRepository boardDefinitionRepository;
    
    @Autowired
    private DownloadQuotaService downloadQuotaService;

    @Value("${info.file.upload.path}")
    private String infoFileUploadPath;

    private Path basePath;

    @PostConstruct
    void init() { basePath = Paths.get(infoFileUploadPath); }

    /**
     * 파일 접근 (다운로드 및 미리보기)
     */
    @GetMapping("/upload/info/{boardPath}/{encodedSafeName:.+}")
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> download(
            @PathVariable("boardPath") String boardPath,
            @PathVariable("encodedSafeName") String encodedSafeName,
            WebRequest webRequest) throws Exception {

        /* 1) safeName 복원 */
        String safeName = UriUtils.decode(encodedSafeName, StandardCharsets.UTF_8);

        /* 2) DB 조회 */
        InfoPostAttachment attachment = attachmentRepository.findByFileOriginName(safeName)
                .orElseThrow(() -> new FileNotFoundException(GlobalExceptionHandler.CC + "첨부파일을 찾을 수 없습니다: " + safeName));

        /* 3) 게시판 정보 조회 및 경로 검증 */
        InfoBoardDefinition boardDefinition = boardDefinitionRepository.findById(attachment.getPost().getBoardId())
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "게시판을 찾을 수 없습니다: " + attachment.getPost().getBoardId()));
        
        BoardCode boardCode = boardDefinition.getBoardCode();
        String expectedBoardPath = boardCode.getCode().toLowerCase();
        
        // URL의 boardPath와 실제 게시판 코드가 일치하는지 검증
        if (!expectedBoardPath.equals(boardPath)) {
            throw new IllegalArgumentException(GlobalExceptionHandler.CC + "잘못된 게시판 경로입니다.");
        }

        /* 4) 디스크 파일 경로 구성 */
        File stored = basePath.resolve(expectedBoardPath).resolve(safeName).toFile();
        if (!stored.exists()) {
            throw new FileNotFoundException(GlobalExceptionHandler.CC + "파일을 찾을 수 없습니다: " + safeName);
        }

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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserPrincipal principal) {
            Integer userId = principal.getUserId();
            downloadQuotaService.checkAndIncrementDownloadSize(userId, fileSize);
        }

        /* 7) Content-Disposition & MIME 설정 (원본 파일명 사용, 모든 파일 다운로드) */
        String originalFileName = attachment.getFileName() != null ? attachment.getFileName() : attachment.getFileOriginName();
        String encodedFileName = UriUtils.encode(originalFileName, StandardCharsets.UTF_8);
        String disposition = "attachment" +
                "; filename=\"" + encodedFileName + "\"" +
                "; filename*=UTF-8''" + encodedFileName;

        MediaType media = attachment.getFileType() != null
                ? MediaType.parseMediaType(attachment.getFileType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .lastModified(lastModifiedInstant)
                .eTag("\"" + fileSize + "-" + lastModified + "\"")
                .cacheControl(CacheControl.maxAge(CACHE_MAX_AGE_DAYS, TimeUnit.DAYS))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .contentType(media)
                .body(new FileSystemResource(stored));
    }
}

