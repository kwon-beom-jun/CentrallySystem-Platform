package com.cs.hrm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 파일 접근 핸들러
@Configuration
public class WebConfig implements WebMvcConfigurer {

	// ex) C:/Users/qjawn/Desktop/eclipse/centrally-system/upload/file/sample
    @Value("${file.upload.path}")
    private String fileUploadPath;

    /**
     * 정적 리소스 매핑 (파일 업로드 경로)
     */
//    사용 X -> 컨트롤러 /upload 로 직접 처리
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        // 예: http://localhost:9999/hrm/upload/** 으로 접근하면 noticeFileUploadPath에 매핑됨
//        registry.addResourceHandler("/upload/**")
//                .addResourceLocations("file:" + fileUploadPath + "/");
//    }
}
