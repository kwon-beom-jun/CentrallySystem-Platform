//package com.cs.gateway.log;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.core.publisher.Mono;
//
//@Configuration
//public class LoggingFilterConfiguration {
//
//    private static final Logger log = LoggerFactory.getLogger(LoggingFilterConfiguration.class);
//
//    @Bean
//    public GlobalFilter customLoggingFilter() {
//        return (ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) -> {
//            // 요청 로그 찍기
//            log.info("Gateway Request URI: {}", exchange.getRequest().getURI());
//            // 필터 체인 실행 후 응답 로그 찍기
//            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
//                log.info("Gateway Response Status: {}", exchange.getResponse().getStatusCode());
//            }));
//        };
//    }
//}
//


