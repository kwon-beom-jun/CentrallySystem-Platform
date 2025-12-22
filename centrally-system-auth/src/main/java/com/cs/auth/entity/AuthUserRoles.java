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
@Table(name = "auth_user_roles")
public class AuthUserRoles extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("사용자 ID")
    @JsonBackReference("user_user_role")
    private AuthUser user;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    @Comment("권한 ID")
    @JsonBackReference("role_user_role")
    private AuthRoles role;
}
