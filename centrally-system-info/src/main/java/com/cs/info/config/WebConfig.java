package com.cs.info.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * INFO 서비스 웹 설정
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path}")
    private String fileUploadPath;

    /**
     * 정적 리소스 매핑 (파일 업로드 경로)
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + fileUploadPath + "/");
    }
}

