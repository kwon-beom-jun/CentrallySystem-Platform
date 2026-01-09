package com.cs.core.kafka.event.hrm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDeptTeamChangedEvent {
    private Integer userId;
    private Integer departmentId;
    private String departmentName;
    private Integer teamId;
    private String teamName;
} 