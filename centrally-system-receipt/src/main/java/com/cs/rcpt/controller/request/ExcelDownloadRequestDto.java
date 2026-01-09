package com.cs.rcpt.controller.request;

import lombok.Data;
import java.util.List;

@Data
public class ExcelDownloadRequestDto {
    private String userName;
    private String deptName;
    private String teamName;
    private String position;
    private List<Long> receiptIds;
}
