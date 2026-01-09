package com.cs.core.util;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.web.util.UriUtils;

public final class FileNameUtils {

    /** path 에 넣을 수 없는 문자 → _ 로 치환 */
    private static final Pattern ILLEGAL = Pattern.compile("[\\p{Cntrl}\\\\/:*?\"<>|\\[\\] ()]");
    
    /** yyyyMMddHHmmss 포맷터 (예: 20250528​142530) */
    private static final DateTimeFormatter TS
            = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    /** 업로드용 ‘안전 파일명’ 생성 */
    public static String safeName(String original) {
        String sanitized = ILLEGAL.matcher(original).replaceAll("_");
        String timestamp = LocalDateTime.now().format(TS);
        return timestamp + "_" + UUID.randomUUID() + "_" + sanitized;
    }

    /** URL path segment 인코딩 (space → %20, + 금지) */
    public static String encodePathSegment(String segment) {
        return UriUtils.encodePathSegment(segment, StandardCharsets.UTF_8);
    }

    private FileNameUtils() {}
}
