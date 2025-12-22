package com.cs.hrm.entity;

import com.cs.core.entity.SoftDeleteEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.util.Set;
import java.util.HashSet;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Comment;
import com.cs.hrm.entity.HrmTeams;
import com.cs.hrm.entity.HrmUserProfileImg;
import com.cs.hrm.enums.EmploymentType;

/**
 * Auth “권한 스냅샷” 전용 테이블
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "hrm_users")
@SQLDelete(sql = "UPDATE hrm_users SET enabled = false, deleted_at = now() WHERE user_id = ?")
public class HrmUser extends SoftDeleteEntity {

	/**
	 * [중요] 
	 * 		Auth 서비스 '임시 사용자' 승인 시 사원 정보(ID 포함)를 Kafka를 통해 보내주고
	 *		받은 사원 정보을 넣어주므로 따로 GeneratedValue를 사용 할 필요 없음
	 *		( Auth ID와 동기화 문제도 있음 )
	 */
//  @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "user_id")
    @Comment("사용자 ID")
    private Integer userId;

    @Column(name = "email", unique = true, nullable = false)
    @Comment("이메일")
    private String email;

//    @Column(name = "password")
//    private String password;

    @Column(name = "name")
    @Comment("이름")
    private String name;

    @Column(name = "birth")
    @Comment("생년월일")
    private String birth;

    @Column(name = "phone_number")
    @Comment("핸드폰 번호")
    private String phoneNumber;

    @Column(name = "address")
    @Comment("주소")
    private String address;

    @Column(name = "address_detail")
    @Comment("주소 상세")
    private String addressDetail;

    @Column(name = "national_id")
    @Comment("주민번호")
    private String nationalId;

    @Column(name = "joining_date")
    @Comment("입사일")
    private Date joiningDate;

    @Column(name = "leaving_date")
    @Comment("퇴사일")
    private Date leavingDate;

    @Column(name = "bank_account_number")
    @Comment("계좌번호")
    private String bankAccountNumber;

    @Column(name = "dispatch_locations")
    @Comment("파견 위치")
    private String dispatchLocations;

    @Column(name = "zip_code")
    @Comment("우편번호")
    private Integer zipCode;

//    @Column(name = "profile_img_id")
//    @Comment("프로필이미지 ID")
//    private Integer profileImgId;

    /* 프로필 이미지 1:1 매핑 (사용자당 한 이미지, 없으면 NULL) */
//    @OneToOne(fetch = FetchType.EAGER, optional = true)
//    @JoinColumn(name = "profile_img_id")
//    @Comment("이미지 ID")
//    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "profile_img_id")
    @Comment("프로필 이미지 ID")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private HrmUserProfileImg profileImg;

    /**
     * 🔥 2) 직책(HrmPositions) 단방향 참조
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = true)
    @Comment("직책 ID")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private HrmPositions position;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "employment_type_id", nullable = false)
    @Comment("고용형태 ID")
    private EmploymentType employmentTypeId;

    /**
     * 🔥 기존의 "Team_ID" 단순 컬럼 대신,
     *    팀 엔티티(HrmTeams)와 ManyToOne 관계로 맵핑
     *    팀 삭제 시 이 FK 를 NULL 로 자동 세팅
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(
        name = "team_id",
        nullable = true,
        foreignKey = @ForeignKey(
            name = "fk_hrm_users_team",
            foreignKeyDefinition =
                "FOREIGN KEY (team_id) REFERENCES hrm_teams(team_id) ON DELETE SET NULL" // FK DDL
        )
    )
    @OnDelete(action = OnDeleteAction.SET_NULL) // 팀 삭제 → team_id = NULL
    @NotFound(action = NotFoundAction.IGNORE)
    @Comment("팀 ID")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "users"})
    private HrmTeams team;
    
    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
	@Builder.Default
	@JsonManagedReference("user_userRole_hrm")
	private Set<HrmUserRoles> userRoles = new HashSet<>();
}
