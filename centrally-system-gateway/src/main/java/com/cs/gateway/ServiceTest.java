package com.cs.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceTest {
    @GetMapping("/centrally-system-service-test")
    public String hello() {
        return "Hello from Service Gateway!";
    }
}
