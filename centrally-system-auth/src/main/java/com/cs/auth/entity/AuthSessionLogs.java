package com.cs.auth.entity;

import com.cs.core.entity.AuditTimeEntity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "auth_session_logs")
public class AuthSessionLogs extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    @Comment("로그 ID")
    private Integer logId;

    @Column(name = "user_id")
    @Comment("사용자 ID")
    private Integer userId;

    @Column(name = "timestamp")
    @Comment("시간")
    private java.sql.Timestamp timestamp;
}
