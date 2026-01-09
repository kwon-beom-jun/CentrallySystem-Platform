package com.cs.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {
	    "com.cs.core", // 코어 모듈 패키지
	    "com.cs.auth"   // 자체 패키지
	})
public class CentrallySystemAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(CentrallySystemAuthApplication.class, args);
	}

}
