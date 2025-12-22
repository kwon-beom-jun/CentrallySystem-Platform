package com.cs.auth.controller.response;

import com.cs.auth.entity.AuthSocialLogin;
import com.cs.auth.entity.AuthUser;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AuthUserSocialResponse {
    private AuthUser user;
    private List<AuthSocialLogin> socialLogins;
}
