package com.cs.hrm.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
//@AllArgsConstructor // 여러 생성자를 사용하므로 Lombok 어노테이션 대신 직접 작성
public class HrmUserWithRolesDto {
	private Integer userId;
	private String name;
	private List<String> roles;

	public HrmUserWithRolesDto() {}
	
	// 서비스 계층에서 최종적으로 사용할 생성자
	public HrmUserWithRolesDto(Integer userId, String name, List<String> roles) {
		this.userId = userId;
		this.name = name;
		this.roles = roles;
	}

	// Repository의 JPQL에서 사용할 생성자
	public HrmUserWithRolesDto(Integer userId, String name, String role) {
		this.userId = userId;
		this.name = name;
		this.roles = new ArrayList<>();
		if (role != null) {
			this.roles.add(role);
		}
	}
}