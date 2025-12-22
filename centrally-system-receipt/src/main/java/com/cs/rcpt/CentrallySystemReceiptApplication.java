package com.cs.rcpt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(scanBasePackages = {
	    "com.cs.core", // 코어 모듈 패키지
	    "com.cs.rcpt"   // 자체 패키지
	})
public class CentrallySystemReceiptApplication {

	public static void main(String[] args) {
		SpringApplication.run(CentrallySystemReceiptApplication.class, args);
	}

}
