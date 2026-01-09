package com.cs.rcpt.service;

import com.cs.rcpt.controller.request.CeoReportRequestDto;
import com.cs.rcpt.controller.request.ExcelDownloadRequestDto;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 영수증 관련 Excel 생성을 담당하는 서비스 인터페이스
 */
public interface ReceiptExcelService {

    /**
     * ID 목록을 받아 해당하는 영수증들의 상세 내역을 담은 Excel 파일을 생성합니다.
     * @param requestDto Excel 다운로드 요청 DTO
     * @return 생성된 Excel 파일의 데이터를 담은 ByteArrayInputStream
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    ByteArrayInputStream generateApprovalSummaryExcel(ExcelDownloadRequestDto requestDto) throws IOException;

    /**
     * CEO 보고용 엑셀 파일을 생성합니다.
     * @param requestDto CEO 보고 요청 DTO
     * @return 생성된 Excel 파일의 데이터를 담은 ByteArrayInputStream
     * @throws IOException 파일 처리 중 오류 발생 시
     */
    ByteArrayInputStream generateCeoReportExcel(CeoReportRequestDto requestDto) throws IOException;
}
