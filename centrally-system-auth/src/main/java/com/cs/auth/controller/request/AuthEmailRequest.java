package com.cs.auth.controller.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthEmailRequest {
    private String email;
}
