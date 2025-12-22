package com.cs.core.kafka.group;

/**
 *	접두사는 마이크로서비스의 컨슈머 기준
 *		ex) AUTH_GROUP : AUTH가 컨슈머(받는쪽)
 */
public class Group {

	public static final String AUTH_GROUP = "auth-group";

	public static final String HRM_GROUP = "hrm-group";
	
	public static final String RECEIPT_GROUP = "receipt-group";
	
	public static final String INFO_GROUP = "info-group";
	
}
