package com.cs.core.vo.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthPermissionEvent {
    private Integer userId;
    private String  serviceName;
    private String  roleName;      // Enum 코드
    private String  type;   // created | updated | deleted
}