package com.cs.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import com.cs.core.entity.AuditTimeEntity;

import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "auth_temp_users")
public class AuthTempUser extends AuditTimeEntity {

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

    @Column(name = "phone_number")
    @Comment("핸드폰 번호")
    private String phoneNumber;

}
