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
@Table(name = "auth_screens")
public class AuthScreens extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    @Comment("메뉴 ID")
    private Integer menuId;

    @Column(name = "menu_name")
    @Comment("메뉴 이름")
    private String menuName;

    /**
     * auth_screens_roles 테이블을 @ManyToMany로 매핑
     * (Role_ID <-> Menu_ID)
     *
     * mappedBy="screens" : AuthRoles 쪽 필드명과 일치
     */
//    @ManyToMany(mappedBy = "screens")
//    @Builder.Default
//    private Set<AuthRoles> roles = new HashSet<>();

    // 기존 ManyToMany -> 매핑 엔티티(AuthScreensRoles)로 변경 (레퍼런스 이름 "screen-screensRole" 지정)
    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference("screen_screens_role")
    private Set<AuthScreensRoles> screensRoles = new HashSet<>();
}
