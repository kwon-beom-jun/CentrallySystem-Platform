package com.cs.gateway.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * R2DBC Entity
 * - PostgreSQL의 gateway_user_activity_log 테이블과 매핑
 */
@Data
@Table("gateway_user_activity_log")
public class UserActivityLog {

    @Id
    private Long activityId;     // SERIAL PRIMARY KEY
    
    private String username;         // 유저 ID (헤더나 세션 등에서 추출)
    
    private String userEmail;         // 유저 ID (헤더나 세션 등에서 추출)
    
    private String menu;         // X-Menu 헤더
    
    private String function;     // X-Func 헤더
    
    private String ip;           // 요청 IP
    
    private String httpMethod;   // GET/POST/PUT/DELETE 등
    
    private String httpBody;   // BODY 추출
    
    private String browser;      // 브라우저 정보 (Chrome, Safari, Firefox 등)
    
    private String os;           // 운영체제 정보 (Windows, MacOS, Linux 등)
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
}
