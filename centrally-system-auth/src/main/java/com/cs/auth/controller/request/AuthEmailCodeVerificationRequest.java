package com.cs.auth.controller.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthEmailCodeVerificationRequest {
    private String email;
    private String code;
}
