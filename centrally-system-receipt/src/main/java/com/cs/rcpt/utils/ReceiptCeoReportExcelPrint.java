package com.cs.rcpt.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.cs.rcpt.controller.request.CeoReportRequestDto;
import com.cs.rcpt.controller.request.ReportRowDto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class ReceiptCeoReportExcelPrint {

    public byte[] createCeoReportExcel(CeoReportRequestDto requestDto) throws IOException {

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(requestDto.getMonth() + " 정산 요약");
            List<ReportRowDto> data = requestDto.getData();

            // 1. 스타일 정의 (폰트 통일)
            Font defaultFont = workbook.createFont();
            defaultFont.setFontName("맑은 고딕");
            defaultFont.setFontHeightInPoints((short) 11);

            Font boldFont = workbook.createFont();
            boldFont.setFontName("맑은 고딕");
            boldFont.setBold(true);

            Font titleFont = workbook.createFont();
            titleFont.setFontName("맑은 고딕");
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 20);

            Font subtitleFont = workbook.createFont();
            subtitleFont.setFontName("맑은 고딕");
            subtitleFont.setFontHeightInPoints((short) 13);
            
            CellStyle titleStyle = createCellStyle(workbook, titleFont, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
            CellStyle subtitleStyle = createCellStyle(workbook, subtitleFont, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
            
            boldFont.setFontHeightInPoints((short) 11);
            CellStyle headerStyle = createCellStyle(workbook, boldFont, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            DataFormat format = workbook.createDataFormat();
            CellStyle currencyStyle = createCellStyle(workbook, defaultFont, HorizontalAlignment.RIGHT, null);
            currencyStyle.setDataFormat(format.getFormat("#,##0"));

            CellStyle totalLabelStyle = createCellStyle(workbook, boldFont, HorizontalAlignment.CENTER, null);
            totalLabelStyle.setBorderTop(BorderStyle.THIN);

            CellStyle totalValueStyle = createCellStyle(workbook, boldFont, HorizontalAlignment.RIGHT, null);
            totalValueStyle.setBorderTop(BorderStyle.THIN);
            totalValueStyle.setDataFormat(format.getFormat("#,##0"));

            // 2. 레이아웃 생성
            createStyledRow(sheet, 1, 30, "영수증 정산 보고", titleStyle, 0, 5);
            createStyledRow(sheet, 2, 20, requestDto.getMonth(), subtitleStyle, 0, 5);
            
            Row headerRow = sheet.createRow(4);
            String[] headers = {"성명", "부서", "팀", "이메일", "총 건수", "합계 승인 금액"};
            for (int i = 0; i < headers.length; i++) {
                createStyledCell(headerRow, i, headers[i], headerStyle);
            }

            int rowNum = 4;
            for (ReportRowDto rowData : data) {
                Row row = sheet.createRow(++rowNum);
                createStyledCell(row, 0, rowData.getName(), null);
                createStyledCell(row, 1, rowData.getDepartment(), null);
                createStyledCell(row, 2, rowData.getTeam(), null);
                createStyledCell(row, 3, rowData.getEmail(), null);
                createStyledCell(row, 4, rowData.getCount(), currencyStyle);
                createStyledCell(row, 5, rowData.getApproved(), currencyStyle);
            }

            rowNum++;
            long totalCount = data.stream().mapToLong(ReportRowDto::getCount).sum();
            long totalApproved = data.stream().mapToLong(ReportRowDto::getApproved).sum();
            Row totalRow = sheet.createRow(rowNum);

            for(int i = 0; i < headers.length; i++) {
                if (i == 3) {
                    createStyledCell(totalRow, i, "총 합계", totalLabelStyle);
                } else if (i == 4) {
                    createStyledCell(totalRow, i, totalCount, totalValueStyle);
                } else if (i == 5) {
                    createStyledCell(totalRow, i, totalApproved, totalValueStyle);
                } else {
                    createStyledCell(totalRow, i, "", totalLabelStyle); 
                }
            }
            
            // 3. 컬럼 너비 설정 (✨ 수정된 부분)
            for (int i = 0; i < headers.length; i++) {
                if (i == 3) { // 3은 '이메일' 컬럼의 0-based 인덱스
                    sheet.setColumnWidth(i, 27 * 256);
                } else {
                    sheet.setColumnWidth(i, 20 * 256);
                }
            }

            // 4. 인쇄 설정 추가 (A4 용지에 맞춤)
            PrintSetup printSetup = sheet.getPrintSetup();
            printSetup.setPaperSize(PrintSetup.A4_PAPERSIZE); // 용지를 A4로 설정
            sheet.setFitToPage(true); // 페이지 맞춤 기능 활성화
            printSetup.setFitWidth((short) 1); // 모든 열을 1페이지 너비에 맞춤
            printSetup.setFitHeight((short) 0); // 높이는 데이터 길이에 따라 여러 페이지가 될 수 있도록 설정

            // 5. 결과 반환
            workbook.write(out);
            return out.toByteArray();
        }
    }
    
    // 스타일 생성을 위한 헬퍼 메서드
    private CellStyle createCellStyle(Workbook workbook, Font font, HorizontalAlignment hAlign, VerticalAlignment vAlign) {
        CellStyle style = workbook.createCellStyle();
        if (font != null) style.setFont(font);
        if (hAlign != null) style.setAlignment(hAlign);
        if (vAlign != null) style.setVerticalAlignment(vAlign);
        return style;
    }
    
    // 행 및 셀 생성을 위한 헬퍼 메서드
    private void createStyledRow(Sheet sheet, int rowNum, float height, String text, CellStyle style, int firstCol, int lastCol) {
        Row row = sheet.createRow(rowNum);
        row.setHeightInPoints(height);
        Cell cell = row.createCell(firstCol);
        cell.setCellValue(text);
        cell.setCellStyle(style);
        if (firstCol != lastCol) {
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, firstCol, lastCol));
        }
    }
    
    // 셀 생성 및 스타일 적용을 위한 헬퍼 메서드
    private void createStyledCell(Row row, int colNum, Object value, CellStyle style) {
        Cell cell = row.createCell(colNum);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        }
        if (style != null) {
            cell.setCellStyle(style);
        }
    }
}