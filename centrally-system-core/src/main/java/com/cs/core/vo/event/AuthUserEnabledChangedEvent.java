package com.cs.core.vo.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUserEnabledChangedEvent {
    private Integer userId;
    private Boolean enabled;
} 