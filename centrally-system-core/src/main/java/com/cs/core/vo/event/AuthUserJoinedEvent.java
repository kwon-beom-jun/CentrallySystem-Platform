package com.cs.core.vo.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Auth에서 새 유저 가입 시, HRM으로 전달할 이벤트
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserJoinedEvent {
    private Integer userId;         // AuthUser의 userId
    private String email;
    private String password;
    private String name;
    private String phone;           // 핸드폰
}
