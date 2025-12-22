package com.cs.rcpt.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.rcpt.controller.response.ReceiptPage;
import com.cs.rcpt.entity.Receipt;
import com.cs.rcpt.entity.ReceiptParticipants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ReceiptConverter {

    @Value("${receipt.file.upload.path}")
    private String receiptFileUploadPath;

    @Value("${receipt.file.upload.url}")
    private String receiptFileUploadUrl;
    
    private ReceiptConverter() {}   // 유틸 클래스 생성 금지

    /* --------------------------------------------------
     * 내부 헬퍼
     * -------------------------------------------------- */

    public static ReceiptPage<Receipt> toReceiptPage(Page<Receipt> pg) {
        return ReceiptPage.<Receipt>builder()
                .content(pg.getContent())
                .totalPages(pg.getTotalPages())
                .totalElements(pg.getTotalElements())
                .pageNumber(pg.getNumber())
                .pageSize(pg.getSize())
                .build();
    }

    /**
     * participants JSON → List<ReceiptParticipants>
     *  - INTERNAL: userId 필수
     *  - EXTERNAL: userId 없이 name/company/position/phone 허용
     *  - Soft-delete 대응을 위해 Receipt owner 를 셋팅한다.
     */
    public static List<ReceiptParticipants> parseParticipants(String json, Receipt owner) {
        try {
            List<Map<String, Object>> arr =
                new ObjectMapper().readValue(json, new TypeReference<>() {});

            return arr.stream()
                      .map(m -> {
                          String type = (String) m.getOrDefault("type", "INTERNAL");
                          if ("EXTERNAL".equalsIgnoreCase(type)) {
                              return ReceiptParticipants.builder()
                                      .participantType("EXTERNAL")
                                      .participantUserId(null)
                                      .participantName((String)  m.get("name"))
                                      .company        ((String)  m.get("company"))
                                      .position       ((String)  m.get("position"))
                                      .phone          ((String)  m.get("phone"))
                                      .department     ((String)  m.get("department"))
                                      .team           ((String)  m.get("team"))
                                      .receipt(owner)
                                      .build();
                          } else {
                              Integer uid = (Integer) m.get("userId");
                              if (uid == null) {
                                  throw new IllegalArgumentException("participantUserId(null) – JSON 오류");
                              }
                              return ReceiptParticipants.builder()
                                      .participantType("INTERNAL")
                                      .participantUserId(uid)
                                      .participantName((String)  m.get("name"))
                                      .department     ((String)  m.get("department"))
                                      .team           ((String)  m.get("team"))
                                      .receipt(owner)
                                      .build();
                          }
                      })
                      .toList();

        } catch (Exception e) {
            throw new RuntimeException(
                GlobalExceptionHandler.CC + "participants JSON 파싱 실패", e);
        }
    }


    /* URL 인코더 */
    public static String enc(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
