package com.cs.gateway.service;

import com.cs.gateway.dao.dto.ActivityLogPage;
import com.cs.gateway.entity.UserActivityLog;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 사용자 활동 로그 서비스 인터페이스
 */
public interface UserActivityLogService {

    /**
     * 활동 로그 조회 (페이지네이션)
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param searchData 검색어
     * @param currentPage 현재 페이지
     * @param pageSize 페이지 크기
     * @return 활동 로그 페이지
     */
    Mono<ActivityLogPage> getActivityLogs(String startDate, String endDate, String searchData, int currentPage, int pageSize);

    /**
     * 특정 사용자의 개인 활동 이력 조회 (페이지네이션)
     * @param userEmail 사용자 이메일
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @param searchData 검색어
     * @param currentPage 현재 페이지
     * @param pageSize 페이지 크기
     * @return 개인 활동 이력 페이지
     */
    Mono<ActivityLogPage> getPersonalHistory(String userEmail, String startDate, String endDate, String searchData, int currentPage, int pageSize);

    /**
     * 특정 사용자의 최신 활동 이력 조회
     * @param userEmail 사용자 이메일
     * @param limit 조회할 최대 개수
     * @return 최신 활동 이력 목록
     */
    Mono<List<UserActivityLog>> getRecentActivities(String userEmail, int limit);
}
