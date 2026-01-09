package com.cs.core.kafka.topic;

/**
 * Receipt 서비스 → 외부 마이크로서비스로 전파되는 이벤트
 */
public final class ReceiptTopics {
    private ReceiptTopics() {}

    public static final String RECEIPT_SUBMITTED = "receipt.submitted";
    public static final String RECEIPT_APPROVED  = "receipt.approved";
} 