package com.cs.core.kafka.event.hrm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 팀 생성 이벤트
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamCreatedEvent {
    private Integer teamId;
    private String teamName;
    private Integer departmentId;
    private String departmentName;
}

