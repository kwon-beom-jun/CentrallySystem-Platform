package com.cs.hrm.controller;

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
import com.cs.hrm.entity.HrmUserProfileImg;
import com.cs.hrm.repository.HrmUserProfileImgRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HRM 첨부파일 접근 컨트롤러 (다운로드 및 미리보기)
 * - 프로필 이미지 등
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class HrmFileController {

    /** 캐시 유지 기간 (일) */
    private static final long CACHE_MAX_AGE_DAYS = 7L;

    private final HrmUserProfileImgRepository profileImgRepository;
    
    @Autowired
    private DownloadQuotaService downloadQuotaService;

    @Value("${hrm.profile.upload.path}")
    private String profileUploadPath;

    private Path basePath;

    /**
     * 파일 업로드 경로 초기화
     */
    @PostConstruct
    void init() {
        basePath = Paths.get(profileUploadPath);
    }

    /**
     * 저장된 파일명으로 파일 경로 반환
     */
    private File resolve(String fileName) {
        return basePath.resolve(fileName).toFile();
    }

    /**
     * HRM 첨부파일 접근 (게이트웨이를 거쳐 접근)
     * - 프로필 이미지: 브라우저에서 미리보기 (inline)
     * - 일반 파일: 다운로드 (attachment)
     * - 원본 파일명으로 다운로드
     */
    @GetMapping("/upload/hrm/profile/{encodedFileName:.+}")
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> download(
            @PathVariable("encodedFileName") String encodedFileName,
            WebRequest webRequest) throws Exception {

        log.debug("HrmFileController.download called with encodedFileName: {}", encodedFileName);
        
        /* 1) fileName 복원 */
        String fileName = UriUtils.decode(encodedFileName, StandardCharsets.UTF_8);
        log.debug("Decoded fileName: {}", fileName);

        /* 2) DB 조회 - fileName으로 먼저 시도, 없으면 fileUrl로 시도 (기존 데이터 호환) */
        HrmUserProfileImg img = profileImgRepository.findByFileName(fileName)
                .orElseGet(() -> {
                    // 기존 데이터 호환: fileUrl로 조회 시도
                    // URL: /upload/hrm/profile/random/003.png -> fileUrl: /upload/hrm/profile/random/003.png 또는 /img/profile/random/003.png
                    String fileUrl = "/upload/hrm/profile/" + fileName;
                    return profileImgRepository.findByFileUrl(fileUrl)
                            .orElseGet(() -> {
                                // 더 오래된 형식: /img/profile/random/003.png
                                String oldFormatUrl = "/img/profile/" + fileName;
                                return profileImgRepository.findByFileUrl(oldFormatUrl).orElse(null);
                            });
                });

        if (img == null) {
            throw new FileNotFoundException(GlobalExceptionHandler.CC + "프로필 이미지를 찾을 수 없습니다: " + fileName);
        }

        /* 3) 디스크 파일 */
        File stored = resolve(fileName);
        if (!stored.exists()) {
            throw new FileNotFoundException(GlobalExceptionHandler.CC + "파일을 찾을 수 없습니다: " + stored.getAbsolutePath());
        }

        /* 4) 캐싱 처리 (Spring의 checkNotModified 사용) */
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

        /* 5) 다운로드 용량 제한 확인 (304 응답이 아닐 때만) */
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserPrincipal principal) {
            Integer userId = principal.getUserId();
            downloadQuotaService.checkAndIncrementDownloadSize(userId, fileSize);
        }

        /* 6) Content-Disposition & MIME 설정 */
        // 이미지 파일 여부 확인
        boolean isImage = img.getFileType() != null &&
                         img.getFileType().toLowerCase().startsWith("image/");

        // 이미지면 "inline" (브라우저에서 미리보기), 일반 파일이면 "attachment" (다운로드)
        // 원본 파일명 우선 사용 (fileName이 원본 파일명, fileOriginName은 UUID가 붙은 저장 파일명)
        String originalFileName = img.getFileName() != null ? img.getFileName() : 
                                  (img.getFileOriginName() != null ? img.getFileOriginName() : fileName);
        String encodedOriginalFileName = UriUtils.encode(originalFileName, StandardCharsets.UTF_8);
        String disposition = (isImage ? "inline" : "attachment") +
                "; filename=\"" + encodedOriginalFileName + "\"" +
                "; filename*=UTF-8''" + encodedOriginalFileName;

        // 이미지면 원본 MIME 타입 사용 (미리보기), 일반 파일이면 APPLICATION_OCTET_STREAM (다운로드)
        MediaType media = isImage
                ? MediaType.parseMediaType(img.getFileType())
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

