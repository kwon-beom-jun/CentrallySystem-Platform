package com.cs.rcpt.enums;          // 경로는 프로젝트 구조에 맞게 조정

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 영수증 결재 절차 상태
 *
 * <pre>
 * 1. REQUEST  – 신청(결재 진행 중)
 * 2. APPROVED – 승인(전원 승인 완료)
 * 3. REJECTED – 반려(결재 도중 반려)
 * 4. WAITING  – 대기(결재 시작 전)
 * 5. CLOSED   – 마감(지급까지 완료)
 * </pre>
 */
@Getter
@RequiredArgsConstructor
public enum ReceiptProcessStatus {

    REQUEST (1, "신청"),
    APPROVED(2, "승인"),
    REJECTED(3, "반려"),
    WAITING (4, "대기"),
    CLOSED  (5, "마감");

    /** DB에 저장되는 코드 */
    private final int code;

    /** 사용자용 한글 설명 */
    private final String description;

    /** 숫자(“3”) → enum */
    public static ReceiptProcessStatus of(int code) {
        return Arrays.stream(values())
                     .filter(e -> e.code == code)
                     .findFirst()
                     .orElseThrow(() ->
                         new IllegalArgumentException("Unknown code=" + code));
    }

    /* ─────────── 정적 매핑/유틸 ─────────── */

    private static final Map<Integer, ReceiptProcessStatus> CODE_MAP =
        Arrays.stream(values())
              .collect(Collectors.toMap(ReceiptProcessStatus::getCode, Function.identity()));

    /** 숫자 코드 → Enum */
    public static ReceiptProcessStatus fromCode(int code) {
        return Optional.ofNullable(CODE_MAP.get(code))
                       .orElseThrow(() -> new IllegalArgumentException("Unknown ReceiptProcessStatus code: " + code));
    }

    /** 결재가 진행 중인가? (REQUEST 단계) */
    public boolean inProgress() {
        return this == REQUEST;
    }

    /** 최종 단계인가? (APPROVED / REJECTED / CLOSED) */
    public boolean isFinal() {
        return this == APPROVED || this == REJECTED || this == CLOSED;
    }
}
