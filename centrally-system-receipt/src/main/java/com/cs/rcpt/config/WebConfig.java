package com.cs.rcpt.config;

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

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 예: http://localhost:9999/receipt/upload/** 으로 접근하면 noticeFileUploadPath에 매핑됨
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + fileUploadPath + "/");
    }
}
