package com.cs.rcpt.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TeamUpdateRequest {
    @NotBlank
    private String teamName;
}