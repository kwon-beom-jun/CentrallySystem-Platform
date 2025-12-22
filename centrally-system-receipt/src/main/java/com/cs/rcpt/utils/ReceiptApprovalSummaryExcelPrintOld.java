//package com.cs.rcpt.utils;
//
//import com.cs.rcpt.entity.Receipt;
//import com.cs.rcpt.entity.ReceiptApprovalLine;
//import com.cs.rcpt.entity.ReceiptParticipants;
//import com.cs.rcpt.service.request.ExcelDownloadRequestDto;
//
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.ss.util.CellRangeAddress;
//import org.apache.poi.ss.util.RegionUtil;
//import org.apache.poi.util.Units;
//import org.apache.poi.xssf.usermodel.XSSFColor;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.awt.Color;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.text.SimpleDateFormat;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
///**
// * Receipt 엔티티 목록을 받아 Excel A4 양식을 생성하는 유틸리티 클래스
// */
//@Component
//public class ReceiptApprovalSummaryExcelPrint {
//
//	// application.properties에서 파일 경로 주입
//	@Value("${receipt.file.upload.path}")
//	private String receiptUploadPath;
//
//	// 로고 경로는 리소스 폴더 등에서 관리하는 것이 이상적
//	@Value("${receipt.excel.logo.path:C:/temp/logo/signature01.png}")
//	private String logoPath;
//
//	// 기안자 정보 (DTO로부터 설정됨)
//	private String userName;
//    private String userDepartment;
//    private String userTeam;
//    private String userPosition;
//	
//	// ===== 레이아웃 상수 =====
//	private static final int TOTAL_COL = 12;
//	private static final int PAGE_BODY_ROWS = 62;
//	private static final int BOX_WIDTH = 2;
//	private static final int BOX_HEIGHT = 4;
//	private static final int INFO_LABEL_COL = 8;
//	private static final int INFO_VALUE_COL1 = 9;
//	private static final int INFO_VALUE_COL2 = 11;
//
//	/**
//	 * 주어진 영수증 목록으로 Excel 파일을 생성하여 byte 배열로 반환합니다.
//	 * @param receipts Excel로 만들 영수증 엔티티 목록
//	 * @param requestDto 요청자 정보가 담긴 DTO
//	 * @return 생성된 Excel 파일의 byte 배열
//	 * @throws IOException 파일 처리 중 오류 발생 시
//	 */
//	public byte[] createReceiptsExcel(List<Receipt> receipts, ExcelDownloadRequestDto requestDto) throws IOException {
//		
//		userName = requestDto.getUserName();
//		userDepartment = requestDto.getDeptName();
//		userTeam = requestDto.getTeamName();
//		userPosition = requestDto.getPosition();
//		
//		try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
//			Sheet sheet = wb.createSheet("A4 Form");
//			for (int c = 0; c < TOTAL_COL; c++)
//				sheet.setColumnWidth(c, 256 * 10);
//
//			CellStyle[] styles = createAllCellStyles(wb);
//
//			int numPages = (int) Math.ceil(receipts.size() / 2.0);
//			for (int page = 0; page < numPages; page++) {
//				int base = page * PAGE_BODY_ROWS;
//				for (int r = base; r < base + PAGE_BODY_ROWS; r++) {
//					if (sheet.getRow(r) == null)
//						sheet.createRow(r).setHeightInPoints(18);
//				}
//
//				placePicture(wb, sheet, logoPath, 0, base, 1, base, Workbook.PICTURE_TYPE_PNG, 1.0, 0);
//				placeInfoSection(sheet, base + 6, styles[0], styles[1]);
//
//				int firstReceiptIndex = page * 2;
//				Receipt receipt1 = receipts.get(firstReceiptIndex);
//				Receipt receipt2 = (firstReceiptIndex + 1 < receipts.size()) ? receipts.get(firstReceiptIndex + 1)
//						: null;
//
//				int bodyTop = base + 10;
//				placeBody(sheet, wb, bodyTop, base, receipt1, receipt2, styles);
//			}
//
//			setupPrint(sheet);
//			wb.setPrintArea(0, 0, TOTAL_COL - 1, 0, sheet.getLastRowNum());
//			wb.write(baos);
//			return baos.toByteArray();
//		}
//	}
//
//	/** 본문 (이미지 + 결재란 + 데이터) */
//	private void placeBody(Sheet sheet, Workbook wb, int top, int base, Receipt receipt1, Receipt receipt2,
//			CellStyle[] styles) throws IOException {
//		int bottom = base + PAGE_BODY_ROWS - 1;
//		final float ROW_HT = 17.2f;
//		for (int r = top; r <= bottom; r++) {
//			if (sheet.getRow(r) != null)
//				sheet.getRow(r).setHeightInPoints(ROW_HT);
//		}
//
//		int midRow = top + 25;
//		int midCol = TOTAL_COL / 2 - 1;
//
//		// --- 1. 상단 그리기 (첫 번째 영수증) ---
//		String imagePath1 = Optional.ofNullable(receipt1.getAttachment())
//				.map(a -> Paths.get(receiptUploadPath, a.getFileOriginName()).toString()).orElse(null);
//		String[] approvers1 = transformApprovers(receipt1.getApprovalLines());
//		String drafter1 = transformDrafter(receipt1);
//		String[][] tableData1 = transformTableData(receipt1);
//
//		drawReceiptContent(sheet, wb, top, midRow, 0, midCol, midCol + 1, TOTAL_COL - 1, imagePath1, approvers1,
//				tableData1, drafter1, styles);
//
//		// --- 2. 하단 그리기 (두 번째 영수증) ---
//		if (receipt2 != null) {
//			String imagePath2 = Optional.ofNullable(receipt2.getAttachment())
//					.map(a -> Paths.get(receiptUploadPath, a.getFileOriginName()).toString()).orElse(null);
//			String[] approvers2 = transformApprovers(receipt2.getApprovalLines());
//			String drafter2 = transformDrafter(receipt2);
//			String[][] tableData2 = transformTableData(receipt2);
//
//			drawReceiptContent(sheet, wb, midRow + 1, bottom, 0, midCol, midCol + 1, TOTAL_COL - 1, imagePath2,
//					approvers2, tableData2, drafter2, styles);
//		} else {
//			// 하단이 비었을 경우 테두리만 그림
//			CellRangeAddress blRegion = new CellRangeAddress(midRow + 1, bottom, 0, midCol);
//			setThinBorder(blRegion, sheet);
//			CellRangeAddress brRegion = new CellRangeAddress(midRow + 1, bottom, midCol + 1, TOTAL_COL - 1);
//			setThinBorder(brRegion, sheet);
//		}
//	}
//
//	/** 특정 영역에 하나의 영수증 컨텐츠를 그리는 메소드 */
//	private void drawReceiptContent(Sheet sheet, Workbook wb, int r1, int r2, int cImg1, int cImg2, int cData1,
//			int cData2, String imagePath, String[] approvers, String[][] tableData, String drafter, CellStyle[] styles)
//			throws IOException {
//		// 이미지 영역
//		CellRangeAddress imgRegion = new CellRangeAddress(r1, r2, cImg1, cImg2);
//		setThinBorder(imgRegion, sheet);
//		if (imagePath != null) {
//			sheet.addMergedRegion(imgRegion);
//			placePicture(wb, sheet, imagePath, cImg1, r1, cImg2, r2, Workbook.PICTURE_TYPE_PNG, 1.0, 0);
//		}
//
//		// 데이터 영역
//		CellRangeAddress dataRegion = new CellRangeAddress(r1, r2, cData1, cData2);
//		setThinBorder(dataRegion, sheet);
//		int approveEndRow = drawApprovalSection(sheet, r1, cData1, cData2, approvers, styles[2], styles[3]);
//		drawInfoTable(sheet, approveEndRow + 1, r2, cData1, cData2, tableData, drafter, styles[4], styles[5], styles[6],
//				styles[2]);
//	}
//
//	/** 데이터 변환: 결재자 정보 */
//	private String[] transformApprovers(List<ReceiptApprovalLine> lines) {
//		if (lines == null)
//			return new String[0];
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd HH:mm");
//		return lines.stream()
//				// approvalRole이 1 (결재)인 경우만 필터링하는 로직 추가
//				.filter(line -> line.getApprovalRole() != null && line.getApprovalRole() == 1)
//				.map(line -> {
//					String position = Optional.ofNullable(line.getDepartment()).orElse("결재자"); // 직위 정보
//					String name = line.getApproverName();
//					String date = Optional.ofNullable(line.getApprovedAt()).map(d -> d.format(formatter)).orElse("해당 영수증에 문제가 있습니다");
//					return String.format("%s\n%s\n(%s)", position, name, date);
//				}).toArray(String[]::new);
//	}
//
//	/**
//	 * 데이터 변환: 기안자 정보
//	 * LocalDateTime 타입인 submissionDate를 올바르게 포맷합니다.
//	 */
//	private String transformDrafter(Receipt receipt) {
//		String drafterName = this.userName;
//		// LocalDateTime을 포맷하기 위해 DateTimeFormatter 사용
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy.MM.dd HH:mm");
//		// receipt.getSubmissionDate()를 올바른 formatter로 포맷
//		String date = Optional.ofNullable(receipt.getRegTime()).map(d -> d.format(formatter)).orElse("");
//		return String.format("%s\n(%s)", drafterName, date);
//	}
//
//	/** 데이터 변환: 테이블 정보 */
//	private String[][] transformTableData(Receipt receipt) {
//		List<String[]> table = new ArrayList<>();
//		
//		// 명단이 없으면 "없음"으로 표시하도록 로직 변경
//		List<ReceiptParticipants> participants = receipt.getParticipantsList();
//		String participantNames;
//		if (participants == null || participants.isEmpty()) {
//			participantNames = "없음";
//		} else {
//			participantNames = participants.stream()
//										   .map(ReceiptParticipants::getParticipantName)
//										   .collect(Collectors.joining(", "));
//		}
//		table.add(new String[] { "명단", participantNames });
//
//		Optional.ofNullable(receipt.getSubmissionDate()).map(date -> new SimpleDateFormat("yyyy.MM.dd").format(date))
//				.ifPresent(dateStr -> table.add(new String[] { "발행 시간", dateStr }));
//
//		Optional.ofNullable(receipt.getAmount()).map(amount -> String.format("%,d원", amount.intValue()))
//				.ifPresent(amountStr -> table.add(new String[] { "금액", amountStr }));
//
//		Optional.ofNullable(receipt.getReason()).filter(reason -> !reason.isEmpty())
//				.ifPresent(reason -> table.add(new String[] { "사유", reason }));
//
//		Optional.ofNullable(receipt.getCategory()).map(cat -> cat.getCategoryName())
//				.ifPresent(catName -> table.add(new String[] { "항목", catName }));
//
//		return table.toArray(new String[0][]);
//	}
//
//	/** 사원 정보란 그리기 */
//	private void placeInfoSection(Sheet sheet, int startRow, CellStyle labelSt, CellStyle valueSt) {
//		String[] labels = { "부서", "이름", "팀", "직책" };
//		String[] vals = { userDepartment, userName, userTeam, userPosition };
//		for (int i = 0; i < labels.length; i++) {
//			createCell(sheet, startRow + i, INFO_LABEL_COL, labels[i], labelSt);
//			CellRangeAddress reg = new CellRangeAddress(startRow + i, startRow + i, INFO_VALUE_COL1, INFO_VALUE_COL2);
//			sheet.addMergedRegion(reg);
//			setThinBorder(reg, sheet);
//			createCell(sheet, startRow + i, INFO_VALUE_COL1, vals[i], valueSt);
//		}
//	}
//
//	/** 결재란 그리기 */
//	private int drawApprovalSection(Sheet sheet, int r1, int c1, int c2, String[] approvers, CellStyle approveSt,
//			CellStyle approveTitleSt) {
//		final int boxCnt = 3;
//		int headerRow = r1;
//		int boxTop = r1 + 1;
//		int boxBottom = boxTop + BOX_HEIGHT - 1;
//		for (int j = 0; j < boxCnt; j++) {
//			int colL = c1 + j * BOX_WIDTH;
//			int colR = colL + BOX_WIDTH - 1;
//			if (colR > c2)
//				continue;
//			if (approvers != null && j < approvers.length) {
//				createCell(sheet, boxTop, colL, approvers[j], approveSt);
//				createCell(sheet, headerRow, colL, approvers[j].split("\n")[0], approveTitleSt);
//			} else {
//				createCell(sheet, boxTop, colL, "", approveSt);
//				createCell(sheet, headerRow, colL, "", approveTitleSt);
//			}
//			CellRangeAddress box = new CellRangeAddress(boxTop, boxBottom, colL, colR);
//			sheet.addMergedRegion(box);
//			setThinBorder(box, sheet);
//			CellRangeAddress ttl = new CellRangeAddress(headerRow, headerRow, colL, colR);
//			sheet.addMergedRegion(ttl);
//			setThinBorder(ttl, sheet);
//		}
//		return boxBottom;
//	}
//
//	/** 데이터 테이블 그리기 */
//	private void drawInfoTable(Sheet sheet, int r1, int r2, int c1, int c2, String[][] infoData, String drafter,
//			CellStyle titleSt, CellStyle labelSt, CellStyle valueSt, CellStyle drafterValueSt) {
//		if (r1 > r2 || infoData == null)
//			return;
//		final int TITLE_ROWS = 3, GROUP_ROWS = 3;
//		int titleEndRow = Math.min(r1 + TITLE_ROWS - 1, r2);
//		CellRangeAddress titleMerge = new CellRangeAddress(r1, titleEndRow, c1, c2);
//		sheet.addMergedRegion(titleMerge);
//		setThinBorder(titleMerge, sheet);
//		createCell(sheet, r1, c1, "영수증 데이터", titleSt);
//
//		int rowPtr = titleEndRow + 1;
//		List<String[]> allInfoData = new ArrayList<>();
//		allInfoData.add(new String[] { "기안", drafter });
//		allInfoData.addAll(Arrays.asList(infoData));
//
//		for (String[] rowData : allInfoData) {
//			int groupEndRow = rowPtr + GROUP_ROWS - 1;
//			if (groupEndRow > r2)
//				break;
//			String label = rowData[0], value = rowData[1];
//			CellStyle currentValStyle = label.equals("기안") ? drafterValueSt : valueSt;
//			createCell(sheet, rowPtr, c1, label, labelSt);
//			CellRangeAddress lblMerge = new CellRangeAddress(rowPtr, groupEndRow, c1, c1);
//			sheet.addMergedRegion(lblMerge);
//			setThinBorder(lblMerge, sheet);
//			createCell(sheet, rowPtr, c1 + 1, value, currentValStyle);
//			CellRangeAddress valMerge = new CellRangeAddress(rowPtr, groupEndRow, c1 + 1, c2);
//			sheet.addMergedRegion(valMerge);
//			setThinBorder(valMerge, sheet);
//			rowPtr = groupEndRow + 1;
//		}
//		if (rowPtr <= r2) {
//			CellRangeAddress remaining = new CellRangeAddress(rowPtr, r2, c1, c2);
//			sheet.addMergedRegion(remaining);
//			setThinBorder(remaining, sheet);
//		}
//	}
//
//	/* ────────── 유틸리티 메소드 ────────── */
//	private Font createFont(Workbook wb, boolean isBold, int height) {
//		Font font = wb.createFont();
//		font.setBold(isBold);
//		font.setFontHeightInPoints((short) height);
//		return font;
//	}
//
//	private CellStyle createCellStyle(Workbook wb, Font font, HorizontalAlignment hAlign, VerticalAlignment vAlign,
//			boolean wrap) {
//		CellStyle style = wb.createCellStyle();
//		style.setFont(font);
//		style.setAlignment(hAlign);
//		style.setVerticalAlignment(vAlign);
//		if (wrap)
//			style.setWrapText(true);
//		setAllThin(style);
//		return style;
//	}
//
//	private CellStyle[] createAllCellStyles(Workbook wb) {
//		CellStyle infoLabelStyle = createCellStyle(wb, createFont(wb, true, 11), HorizontalAlignment.CENTER,
//				VerticalAlignment.CENTER, false);
//		infoLabelStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
//		infoLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//		CellStyle infoValueStyle = createCellStyle(wb, createFont(wb, false, 11), HorizontalAlignment.LEFT,
//				VerticalAlignment.CENTER, false);
//		infoValueStyle.setIndention((short) 1);
//		CellStyle approveSt = createCellStyle(wb, createFont(wb, false, 10), HorizontalAlignment.CENTER,
//				VerticalAlignment.CENTER, true);
//		CellStyle approveTitleSt = createCellStyle(wb, createFont(wb, true, 11), HorizontalAlignment.CENTER,
//				VerticalAlignment.CENTER, false);
//		CellStyle titleStGray = createCellStyle(wb, createFont(wb, true, 13), HorizontalAlignment.CENTER,
//				VerticalAlignment.CENTER, false);
//		titleStGray.setFillForegroundColor(new XSSFColor(new Color(0xF2, 0xF2, 0xF2), null));
//		titleStGray.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//		setAllThin(titleStGray);
//		CellStyle labelCellSt = createCellStyle(wb, createFont(wb, true, 11), HorizontalAlignment.CENTER,
//				VerticalAlignment.CENTER, false);
//		setAllThin(labelCellSt);
//		CellStyle valueCellSt = createCellStyle(wb, createFont(wb, false, 11), HorizontalAlignment.LEFT,
//				VerticalAlignment.CENTER, true);
//		valueCellSt.setIndention((short) 1);
//		setAllThin(valueCellSt);
//		return new CellStyle[] { infoLabelStyle, infoValueStyle, approveSt, approveTitleSt, titleStGray, labelCellSt,
//				valueCellSt };
//	}
//
//	private void setAllThin(CellStyle cs) {
//		cs.setBorderTop(BorderStyle.THIN);
//		cs.setBorderBottom(BorderStyle.THIN);
//		cs.setBorderLeft(BorderStyle.THIN);
//		cs.setBorderRight(BorderStyle.THIN);
//	}
//
//	private void setThinBorder(CellRangeAddress reg, Sheet sheet) {
//		RegionUtil.setBorderTop(BorderStyle.THIN, reg, sheet);
//		RegionUtil.setBorderBottom(BorderStyle.THIN, reg, sheet);
//		RegionUtil.setBorderLeft(BorderStyle.THIN, reg, sheet);
//		RegionUtil.setBorderRight(BorderStyle.THIN, reg, sheet);
//	}
//
//	private void createCell(Sheet sheet, int r, int c, String value, CellStyle style) {
//		Row row = sheet.getRow(r);
//		if (row == null)
//			row = sheet.createRow(r);
//		Cell cell = row.createCell(c);
//		cell.setCellValue(value);
//		cell.setCellStyle(style);
//	}
//
//	private void placePicture(Workbook wb, Sheet sheet, String path, int c1, int r1, int c2, int r2, int type,
//			double scale, int margin) throws IOException {
//		try {
//			Path p = Paths.get(path);
//			if (!Files.exists(p)) {
//				System.err.println("이미지 파일을 찾을 수 없습니다: " + path);
//				return;
//			}
//			byte[] img = Files.readAllBytes(p);
//			int id = wb.addPicture(img, type);
//			ClientAnchor a = wb.getCreationHelper().createClientAnchor();
//			a.setCol1(c1);
//			a.setRow1(r1);
//			a.setCol2(c2 + 1);
//			a.setRow2(r2 + 1);
//			int m = Units.pixelToEMU(margin);
//			a.setDx1(m);
//			a.setDy1(m);
//			sheet.createDrawingPatriarch().createPicture(a, id).resize(scale);
//		} catch (IOException e) {
//			System.err.println("이미지 파일 처리 중 오류 발생: " + path);
//		}
//	}
//
//	private void setupPrint(Sheet sheet) {
//		PrintSetup ps = sheet.getPrintSetup();
//		ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
//		sheet.setFitToPage(true);
//		ps.setFitWidth((short) 1);
//		ps.setFitHeight((short) 0);
//	}
//}
