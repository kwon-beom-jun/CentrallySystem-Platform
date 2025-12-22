package com.cs.rcpt.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptHistoryOverviewDto {

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Period {
		private String startDate;
		private String endDate;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class MonthlySummary {
		private long count;
		private long total;
		private int approvalRate;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Activity {
		private String timestamp;
		private String label;
		private String text;
		private Integer receiptId;
		private String  receiptCode;
		// 신청자 및 표시용 부가 정보
		private Integer applicantUserId;
		private String  applicantUserName;
		private String  applicantUserEmail;
		private String  categoryName;
		private Long    amount;
	}

	private Period period;
	private MonthlySummary monthlySummary;
	private long myPendingCount;
	private List<Activity> recentActivities;
}


