package com.cs.rcpt.controller.request;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BulkRejectAndRemoveRequest {
	private List<Integer> receiptIds;
	private Integer userToRemoveId;
}