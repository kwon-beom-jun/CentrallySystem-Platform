package com.cs.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 
 */
@Configuration
public class R2dbcDatabaseInitializer {
	
    @Value("${database.initializer}")
    private boolean databaseInitializer;
    
    @Value("${database.initializer.path}")
    private String databaseInitializerPath;

    @Bean
    public ApplicationRunner initializeDatabase(DatabaseClient databaseClient, ResourceLoader resourceLoader) {
        return args -> {
        	if (!databaseInitializer) {
				return;
			}
            Resource schemaResource = resourceLoader.getResource(databaseInitializerPath);
            // 스키마 파일을 문자열로 읽습니다.
            String sql = StreamUtils.copyToString(schemaResource.getInputStream(), StandardCharsets.UTF_8);
            // SQL 스크립트를 실행합니다.
            Mono<Long> result = databaseClient.sql(sql)
                    .fetch()
                    .rowsUpdated();
            result.doOnNext(count -> System.out.println("Schema initialized, rows updated: " + count))
                  .subscribe();
        };
    }
}
