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
@Table(name = "auth_screens_roles")
public class AuthScreensRoles extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    @Comment("권한 ID")
    @JsonBackReference("role_screens_role")
    private AuthRoles role;

    @ManyToOne
    @JoinColumn(name = "menu_id", nullable = false)
    @Comment("메뉴 ID")
    @JsonBackReference("screen_screens_role")
    private AuthScreens screen;
}
