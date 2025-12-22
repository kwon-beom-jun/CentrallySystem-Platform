package com.cs.hrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

// 스프링 부트 2.x 이상에서는 @EnableDiscoveryClient 없이도 
// spring-cloud-starter-netflix-eureka-client 의존성을 추가하면 자동으로 등록
// @EnableDiscoveryClient
@EnableScheduling
@SpringBootApplication(scanBasePackages = {
	    "com.cs.core", // 코어 모듈 패키지
	    "com.cs.hrm"   // 자체 패키지
	})
public class CentrallySystemHrmApplication {

	public static void main(String[] args) {
		SpringApplication.run(CentrallySystemHrmApplication.class, args);
	}

}
