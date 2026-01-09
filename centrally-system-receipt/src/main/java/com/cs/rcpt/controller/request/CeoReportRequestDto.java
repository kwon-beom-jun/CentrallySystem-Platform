package com.cs.rcpt.controller.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class CeoReportRequestDto {
    private String month;
    private List<ReportRowDto> data;
}