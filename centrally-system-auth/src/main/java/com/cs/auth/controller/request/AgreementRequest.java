package com.cs.auth.controller.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgreementRequest {
    private Long agreementId;
    private Boolean isAgreed;
} 