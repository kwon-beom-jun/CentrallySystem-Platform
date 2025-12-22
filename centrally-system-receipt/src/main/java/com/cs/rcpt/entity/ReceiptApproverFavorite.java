package com.cs.rcpt.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(
    name = "receipt_approver_favorite",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_receipt_fav_owner_target", columnNames = {"owner_user_id", "favorite_user_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptApproverFavorite {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Comment("PK")
	private Long id;

	@Column(name = "owner_user_id", nullable = false)
	@Comment("즐겨찾기 소유 사용자 ID")
	private Long ownerUserId;

	@Column(name = "favorite_user_id", nullable = false)
	@Comment("즐겨찾기 대상 사용자 ID")
	private Long favoriteUserId;

	@Column(name = "favorite_user_name")
	@Comment("즐겨찾기 대상 사용자 이름")
	private String favoriteUserName;

	@Column(name = "email")
	@Comment("즐겨찾기 대상 사용자 Email")
	private String email;

	@Column(name = "department")
	@Comment("부서")
	private String department;

	@Column(name = "team")
	@Comment("팀")
	private String team;

	@Column(name = "step_no")
	@Comment("정렬 순서")
	private Integer stepNo;

	/** 소유자-대상 조합 유니크 제약조건 보장 (DDL 은 JPA Schema 또는 수동 관리 가정) */
}


