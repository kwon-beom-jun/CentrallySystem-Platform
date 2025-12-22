//package com.cs.hrm.controller.response;
//
///**
// * 기존 방식을 간결하게 사용 할 수 있는 기능
// * Java 14도입, Java 16 이상에서 정식 사용
// * public class HrmUserIdNameDto {
//	    private final Integer userId;
//	    private final String name;
//	
//	    public HrmUserIdNameDto(Integer userId, String name) {
//	        this.userId = userId;
//	        this.name = name;
//	    }
//	
//	    public Integer getUserId() { return userId; }
//	    public String getName() { return name; }
//	}
// */
///** /users/bulk-lookup 전용 : userId, name 두 가지만 */
//public record HrmUserIdNameDto(Integer userId, String name) {
//	
//}
