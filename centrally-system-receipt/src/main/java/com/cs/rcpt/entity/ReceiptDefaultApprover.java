package com.cs.rcpt.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "receipt_default_approver")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptDefaultApprover {

	@Id                         // 🔸 ① PK
	@Column(name = "user_id")
	@Comment("사용자 ID")
	private Long userId;

	@Column(name = "user_name")
	@Comment("사용자 이름")
	private String userName;
	
	@Column(name = "step_no")
	@Comment("순서")
	private Integer stepNo;

	@Column(unique = true)
	@Comment("사용자 Email")
	private String email;

	@Column
	@Comment("부서")
	private String department;

	@Column
	@Comment("팀")
	private String team;
}
