package com.cs.rcpt.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cs.rcpt.controller.request.CeoReportRequestDto;
import com.cs.rcpt.controller.request.ExcelDownloadRequestDto;
import com.cs.rcpt.entity.Receipt;
import com.cs.rcpt.repository.ReceiptRepository;
import com.cs.rcpt.service.ReceiptExcelService;
import com.cs.rcpt.utils.ReceiptCeoReportExcelPrint;
import com.cs.rcpt.utils.ReceiptApprovalSummaryExcelPrint;
import lombok.RequiredArgsConstructor;

/**
 * 영수증 관련 Excel 생성을 담당하는 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class ReceiptExcelServiceImpl implements ReceiptExcelService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptApprovalSummaryExcelPrint approvalSummaryExcelPrint;
    private final ReceiptCeoReportExcelPrint ceoReportExcelPrint;

    /**
     * ID 목록을 받아 해당하는 영수증들의 상세 내역을 담은 Excel 파일을 생성합니다.
     *
     * @param requestDto 생성할 영수증의 ID 목록
     * @return 생성된 Excel 파일의 데이터를 담은 ByteArrayInputStream
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    @Override
    public ByteArrayInputStream generateApprovalSummaryExcel(ExcelDownloadRequestDto requestDto) throws IOException {
        // DTO에서 ID 목록을 가져와 엔티티 조회
        List<Receipt> receipts = receiptRepository.findAllById(
            requestDto.getReceiptIds().stream()
                      .map(Long::intValue)
                      .collect(Collectors.toList())
        );

        // 2. 조회된 엔티티 목록을 Excel 생성 유틸리티에 전달하여 파일 데이터를 생성합니다.
        byte[] excelData = approvalSummaryExcelPrint.createReceiptsExcel(receipts, requestDto);
        
        // 3. 생성된 파일 데이터를 스트림으로 변환하여 반환합니다. (Controller에서 파일 다운로드로 사용)
        return new ByteArrayInputStream(excelData);
    }

    
    /**
     * CEO 보고용 엑셀 파일을 생성합니다.
     * (로직을 유틸리티 클래스에 위임하여 간결화)
     */
    @Override
    public ByteArrayInputStream generateCeoReportExcel(CeoReportRequestDto requestDto) throws IOException {
        // 유틸리티 클래스의 메서드를 호출하여 byte 배열을 받고, 스트림으로 변환하여 반환
        byte[] excelData = ceoReportExcelPrint.createCeoReportExcel(requestDto);
        return new ByteArrayInputStream(excelData);
    }
}

