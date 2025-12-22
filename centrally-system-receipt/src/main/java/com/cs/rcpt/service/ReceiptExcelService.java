package com.cs.rcpt.service;

import com.cs.rcpt.controller.request.CeoReportRequestDto;
import com.cs.rcpt.controller.request.ExcelDownloadRequestDto;
import com.cs.rcpt.entity.Receipt;
import com.cs.rcpt.repository.ReceiptRepository;
import com.cs.rcpt.utils.ReceiptCeoReportExcelPrint;
import com.cs.rcpt.utils.ReceiptApprovalSummaryExcelPrint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 영수증 관련 Excel 생성을 담당하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class ReceiptExcelService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptApprovalSummaryExcelPrint approvalSummaryExcelPrint;
    private final ReceiptCeoReportExcelPrint ceoReportExcelPrint;

    /**
     * ID 목록을 받아 해당하는 영수증들의 상세 내역을 담은 Excel 파일을 생성합니다.
     *
     * @param receiptIds 생성할 영수증의 ID 목록
     * @return 생성된 Excel 파일의 데이터를 담은 ByteArrayInputStream
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    public ByteArrayInputStream generateApprovalSummaryExcel(ExcelDownloadRequestDto requestDto) throws IOException {
        // 포트폴리오용 - 구현 제거됨
        // 기능: 승인 완료 영수증 엑셀 다운로드 파일 생성
        // - 영수증 ID 목록으로 엔티티 조회
        // - Excel 생성 유틸리티를 통한 파일 데이터 생성 (ReceiptApprovalSummaryExcelPrint)
        // - ByteArrayInputStream 변환
        throw new RuntimeException("포트폴리오용으로 구현이 제거되었습니다.");
    }

    
    /**
     * CEO 보고용 엑셀 파일을 생성합니다.
     * (로직을 유틸리티 클래스에 위임하여 간결화)
     */
    public ByteArrayInputStream generateCeoReportExcel(CeoReportRequestDto requestDto) throws IOException {
        // 포트폴리오용 - 구현 제거됨
        // 기능: CEO 보고용 엑셀 파일 생성
        // - Excel 생성 유틸리티를 통한 CEO 리포트 데이터 생성 (ReceiptCeoReportExcelPrint)
        // - ByteArrayInputStream 변환
        throw new RuntimeException("포트폴리오용으로 구현이 제거되었습니다.");
    }
    
    
    
    
}