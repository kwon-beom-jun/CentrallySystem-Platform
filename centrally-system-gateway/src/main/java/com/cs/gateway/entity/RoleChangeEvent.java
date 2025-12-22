package com.cs.gateway.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import io.r2dbc.postgresql.codec.Json;   // ★

import lombok.Data;

@Data
@Table("gateway_role_change_event")
public class RoleChangeEvent {

    @Id
    private Long eventId;
    private Long userId;
    private String userEmail;
    private String service;
    private String roleName;
//    private String action;
    private String httpMethod;
    private Json payload;
    private LocalDateTime createdAt;
    private LocalDateTime checkedAt;
}
