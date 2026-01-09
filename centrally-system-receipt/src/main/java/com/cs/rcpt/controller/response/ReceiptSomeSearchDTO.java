package com.cs.rcpt.controller.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReceiptSomeSearchDTO {
    private String name;
    private String department;
    private String team;
    private String email;
    private int count;
    private long requested;
}
