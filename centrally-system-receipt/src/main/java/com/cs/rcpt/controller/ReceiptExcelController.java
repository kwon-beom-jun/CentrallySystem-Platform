package com.cs.rcpt.controller;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.rcpt.controller.request.CeoReportRequestDto;
import com.cs.rcpt.controller.request.ExcelDownloadRequestDto;
import com.cs.rcpt.service.ReceiptDefaultApproverService;
import com.cs.rcpt.service.ReceiptExcelService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/excel")
@RequiredArgsConstructor
public class ReceiptExcelController {

    private final ReceiptExcelService excelService;

    /**
     * 승인(마감) 상세 엑셀 다운로드
     * POST /excel/{userId}/approval/summary
     */
    @PostMapping("/{userId}/approval/summary")
    public ResponseEntity<InputStreamResource> downloadApprovalSummary(
            @PathVariable("userId") Integer userId,
            @RequestBody ExcelDownloadRequestDto requestDto
    ) {
        try {
            ByteArrayInputStream in = excelService.generateApprovalSummaryExcel(requestDto);

            // 1. DTO에서 userName 가져오기
            String userName = requestDto.getUserName();
            if (userName == null || userName.trim().isEmpty()) {
                userName = "사용자"; // 이름이 없는 경우 기본값 설정
            }

            // 2. 동적 파일명 생성 및 UTF-8 인코딩
            String fileName = "영수증_승인(마감)_상세_" + userName + ".xlsx";
            String encodedFileName = URLEncoder.encode(
            		fileName, 
            		StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                    .filename(encodedFileName) // 인코딩된 파일명 사용
                    .build()
            );
            
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

            return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));

        } catch (IOException e) {
            log.error("Excel 다운로드 실패: {}", e.getMessage());
            throw new RuntimeException(GlobalExceptionHandler.CC + "엑셀 다운로드에 실패하였습니다");
        }
    }
    


    /**
     * CEO 보고용 영수증 정산 요약 엑셀 다운로드
     * POST /excel/ceo-report
     */
    @PostMapping("/ceo-report")
    public ResponseEntity<InputStreamResource> downloadCeoReport(
        @RequestBody CeoReportRequestDto requestDto
    ) {
        try {
            // 서비스에 DTO를 그대로 전달
            ByteArrayInputStream in = excelService.generateCeoReportExcel(requestDto);

            HttpHeaders headers = new HttpHeaders();
            String encodedFileName = URLEncoder.encode(
                "보고용_영수증_정산_" + requestDto.getMonth() + ".xlsx", "UTF-8").replaceAll("\\+", "%20");
            
            headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                                  .filename(encodedFileName)
                                  .build()
            );
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

            return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));

        } catch (IOException e) {
            log.error("CEO 보고용 Excel 다운로드 실패: {}", e.getMessage());
            throw new RuntimeException(GlobalExceptionHandler.CC + "엑셀 다운로드에 실패하였습니다");
        }
    }
}
