package com.cs.core.kafka.event.hrm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 부서 생성 이벤트
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentCreatedEvent {
    private Integer departmentId;
    private String departmentName;
}

