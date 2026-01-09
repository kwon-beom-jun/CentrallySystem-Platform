package com.cs.core.service;

/**
 * 다운로드 용량 제한 서비스 인터페이스
 */
public interface DownloadQuotaService {
    
    /**
     * 사용자별 일일 다운로드 용량 확인 및 증가
     * @param userId 사용자 ID
     * @param fileSize 다운로드할 파일 크기 (바이트)
     * @throws RuntimeException 일일 다운로드 용량 제한 초과 시
     */
    void checkAndIncrementDownloadSize(Integer userId, long fileSize);
    
    /**
     * 현재 사용자의 일일 다운로드 용량 조회 (MB 단위)
     * @param userId 사용자 ID
     * @return 현재 다운로드 용량 (MB)
     */
    long getCurrentDownloadSizeMB(Integer userId);
}

