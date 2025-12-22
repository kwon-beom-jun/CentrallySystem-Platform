package com.cs.core.kafka.event.hrm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDeletedEvent {
    private Integer departmentId;
} 