package com.cs.core.service.impl;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.core.service.DownloadQuotaService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 다운로드 용량 제한 서비스 (인메모리 구현)
 * - 사용자 ID 기반으로 일일 다운로드 용량 제한
 * - Redis 전환 시 구현체만 교체하면 됨
 */
@Service("downloadQuotaService")
public class InMemoryDownloadQuotaService implements DownloadQuotaService {
    
    // 사용자별 일일 다운로드 용량 저장 (키: "날짜:userId", 값: 바이트)
    private final Map<String, Long> downloadQuotaMap = new ConcurrentHashMap<>();
    
    @Value("${download.quota.daily.limit.mb:1000}")
    private long dailyLimitMB;
    
    private long dailyLimitBytes;
    
    @PostConstruct
    void init() {
        dailyLimitBytes = dailyLimitMB * 1024 * 1024; // MB를 바이트로 변환
    }
    
    /**
     * 사용자별 일일 다운로드 용량 확인 및 증가
     */
    @Override
    public void checkAndIncrementDownloadSize(Integer userId, long fileSize) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID가 필요합니다.");
        }
        
        String key = getTodayKey(userId);
        
        // 현재 다운로드 용량 조회 (없으면 0)
        long currentSize = downloadQuotaMap.getOrDefault(key, 0L);
        
        // 제한 초과 확인
        if (currentSize + fileSize > dailyLimitBytes) {
            long currentMB = currentSize / 1024 / 1024;
            throw new RuntimeException(
                GlobalExceptionHandler.CC + 
                String.format("일일 다운로드 용량 제한을 초과했습니다. (제한: %dMB, 현재: %dMB)", 
                    dailyLimitMB, currentMB)
            );
        }
        
        // 다운로드 용량 증가
        downloadQuotaMap.put(key, currentSize + fileSize);
    }
    
    /**
     * 현재 사용자의 일일 다운로드 용량 조회 (MB 단위)
     */
    @Override
    public long getCurrentDownloadSizeMB(Integer userId) {
        if (userId == null) {
            return 0L;
        }
        String key = getTodayKey(userId);
        long bytes = downloadQuotaMap.getOrDefault(key, 0L);
        return bytes / 1024 / 1024;
    }
    
    /**
     * 오늘 날짜와 사용자 ID를 조합한 키 생성
     */
    private String getTodayKey(Integer userId) {
        return LocalDate.now().toString() + ":" + userId;
    }
    
    /**
     * 매일 자정에 전날 데이터 정리 (메모리 절약)
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정
    public void cleanupOldData() {
        String today = LocalDate.now().toString();
        downloadQuotaMap.entrySet().removeIf(entry -> !entry.getKey().startsWith(today));
    }
}

