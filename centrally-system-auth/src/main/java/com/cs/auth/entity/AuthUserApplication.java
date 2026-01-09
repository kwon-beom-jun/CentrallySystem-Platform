package com.cs.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import com.cs.core.entity.AuditTimeEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "auth_user_application")
public class AuthUserApplication extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자 ID
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("사용자 ID")
    @JsonBackReference("user_user_application") // AuthUser의 userApplications와 매핑되는 back reference
    private AuthUser user;

    // 어플리케이션 ID
    @ManyToOne
    @JoinColumn(name = "application_id", nullable = false)
    @Comment("어플리케이션 ID")
    @JsonBackReference("application_user_application") // AuthApplication의 userApplications와 매핑되는 back reference
    private AuthApplication application;
}
