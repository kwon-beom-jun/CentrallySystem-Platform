package com.cs.rcpt.controller.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproverFavoriteDto {

	private Long id;
	private Long ownerUserId;
	private Long favoriteUserId;
	private String favoriteUserName;
	private String email;
	private String department;
	private String team;
	private Integer stepNo;
}


