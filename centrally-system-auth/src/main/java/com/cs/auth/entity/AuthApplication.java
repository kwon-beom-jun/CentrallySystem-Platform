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
@Table(name = "auth_application")
public class AuthApplication extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    @Comment("어플리케이션 ID")
    private Integer applicationId;

    @Column(name = "application_name")
    @Comment("어플리케이션 이름")
    private String applicationName;

    /**
     * auth_user_application_map 테이블을
     * @ManyToMany로 매핑 (User_ID <-> Application_ID)
     *
     * mappedBy="applications" : AuthUsers 쪽 필드명과 일치
     */
//    @ManyToMany(mappedBy = "applications")
//    @Builder.Default
//    private Set<AuthUsers> users = new HashSet<>();

    // AuthUsers와의 매핑을 매핑 엔티티(AuthUserApplication)로 변경
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference("application_user_application") // reference 이름 지정
    private Set<AuthUserApplication> userApplications = new HashSet<>();
}
