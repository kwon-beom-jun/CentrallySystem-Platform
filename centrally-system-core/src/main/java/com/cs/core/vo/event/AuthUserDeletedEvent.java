package com.cs.core.vo.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**  Auth → HRM : 유저 삭제(비활성화) 알림  */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserDeletedEvent {
    private Integer userId;   // 삭제(비활성화)된 사용자 ID
    private String  email;    // 참고용 이메일
}
