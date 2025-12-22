package com.cs.auth.controller.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthJoinRequest {
    private String userName;
    private String email;
    private String phone;
    private String dob;
    private String joinDate;
//    private String department;
//    private String team;
//    private String position;
    private Integer zipCode;
    private String address;
    private String detailAddress;
    private String password;
    private String verificationCode;

    // 약관 동의 정보
    private List<AgreementRequest> agreements;
    private String agreementLanguageCode;
}
