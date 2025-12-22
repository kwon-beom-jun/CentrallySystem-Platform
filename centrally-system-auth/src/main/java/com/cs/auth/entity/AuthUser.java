package com.cs.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import com.cs.core.entity.SoftDeleteEntity;
import com.cs.core.domain.ProcessingStatus;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "auth_users")
@SQLDelete(sql = "UPDATE auth_users SET enabled = false, deleted_at = now() WHERE user_id = ?")
public class AuthUser extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    @Comment("사용자 ID")
    private Integer userId;

    @Column(name = "email", unique = true, nullable = false)
    @Comment("이메일")
    private String email;

    @Column(name = "password", nullable = false)
    @Comment("비밀번호")
    private String password;

    @Column(name = "name")
    @Comment("이름")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Comment("카프카 처리 상태")
    @Builder.Default
    private ProcessingStatus status = ProcessingStatus.PENDING;

    /**
     * 권한(roles) 매핑 (1:N)
     */
    @OneToMany(mappedBy = "user", 
            cascade = CascadeType.ALL, 
            orphanRemoval = true, 
            fetch = FetchType.LAZY)
    @Builder.Default
    private Set<AuthUserRoles> userRoles = new HashSet<>();
}
