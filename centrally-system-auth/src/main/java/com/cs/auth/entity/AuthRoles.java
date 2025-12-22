package com.cs.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

import com.cs.core.entity.AuditTimeEntity;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "auth_roles")
public class AuthRoles extends AuditTimeEntity {

    // 권한 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    @Comment("권한 ID")
    private Integer roleId;

    // 권한 이름
    @Column(name = "role_name")
    @Comment("권한 이름")
    private String roleName;
    
    // 권한 이름 상세
    @Column(name = "role_name_detail")
    @Comment("권한 이름 상세")
    private String roleNameDetail;

    // 서비스 이름
    @Column(name = "service_name")
    @Comment("서비스 이름")
    private String serviceName;

    /**
     * auth_user_roles 테이블을 @ManyToMany로 매핑
     * (User_ID <-> Role_ID)
     * 
     * 실제로는 AuthUsers에서 mappedBy="roles" 형태로 상호 참조
     */
//    @ManyToMany(mappedBy = "roles")
//    @Builder.Default
//    private Set<AuthUsers> users = new HashSet<>();

    /**
     * auth_screens_roles 테이블을 @ManyToMany로 매핑
     * (Role_ID <-> Menu_ID)
     */
//    @ManyToMany
//    @JoinTable(
//        name = "auth_screens_roles",
//        joinColumns = @JoinColumn(name = "Role_ID"),
//        inverseJoinColumns = @JoinColumn(name = "Menu_ID")
//    )
//    @Builder.Default
//    private Set<AuthScreens> screens = new HashSet<>();

    // AuthUsers와의 매핑을 매핑 엔티티(AuthUserRoles)로 변경 (레퍼런스 이름 "role-userRole" 지정)
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference("role_user_role")
    private Set<AuthUserRoles> userRoles = new HashSet<>();

    // AuthScreens와의 매핑을 매핑 엔티티(AuthScreensRoles)로 변경 (레퍼런스 이름 "role-screensRole" 지정)
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference("role_screens_role")
    private Set<AuthScreensRoles> screensRoles = new HashSet<>();
}
