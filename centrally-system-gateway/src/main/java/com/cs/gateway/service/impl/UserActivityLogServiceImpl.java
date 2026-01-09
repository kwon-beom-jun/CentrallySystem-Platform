package com.cs.gateway.service.impl;

import com.cs.gateway.dao.UserActivityLogDao;
import com.cs.gateway.dao.dto.ActivityLogPage;
import com.cs.gateway.entity.UserActivityLog;
import com.cs.gateway.service.UserActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserActivityLogServiceImpl implements UserActivityLogService {

    private final UserActivityLogDao userActivityLogDao;

    @Override
    public Mono<ActivityLogPage> getActivityLogs(String startDate,
                                                 String endDate,
                                                 String searchData,
                                                 int currentPage,
                                                 int pageSize) {
        // 1) 전체 개수
        Mono<Long> countMono = userActivityLogDao.count(startDate, endDate, searchData);

        // 2) 현재 페이지 데이터
        Flux<UserActivityLog> logsFlux = userActivityLogDao.findPage(
                startDate, endDate, searchData, currentPage, pageSize
        );

        // zip: (count, list) -> ActivityLogPage
        return Mono
        	.zip(
	            countMono,
	            logsFlux.collectList())
        	.map(tuple -> {
	            long totalCount = tuple.getT1();
	            List<UserActivityLog> content = tuple.getT2();
	
	            ActivityLogPage page = new ActivityLogPage();
	            page.setContent(content);
	            page.setTotalElements(totalCount);
	            page.setPageNumber(currentPage);
	            page.setPageSize(pageSize);
	
	            int totalPages = (int) Math.ceil((double) totalCount / pageSize);
	            page.setTotalPages(totalPages);
	
	            return page;
	        });
    }

    /**
     * 특정 사용자의 개인 활동 이력 조회 (페이지네이션)
     *
     * @param userEmail 사용자 이메일
     * @param startDate 시작 날짜
     * @param endDate   종료 날짜
     * @param searchData 검색어
     * @param currentPage 현재 페이지
     * @param pageSize 페이지 크기
     * @return 개인 활동 이력 페이지
     */
    @Override
    public Mono<ActivityLogPage> getPersonalHistory(String userEmail,
                                                    String startDate,
                                                    String endDate,
                                                    String searchData,
                                                    int currentPage,
                                                    int pageSize) {
        // 1) 전체 개수
        Mono<Long> countMono = userActivityLogDao.countPersonal(userEmail, startDate, endDate, searchData);

        // 2) 현재 페이지 데이터
        Flux<UserActivityLog> logsFlux = userActivityLogDao.findPersonalPage(
                userEmail, startDate, endDate, searchData, currentPage, pageSize
        );

        // zip: (count, list) -> ActivityLogPage
        return Mono
            .zip(
                countMono,
                logsFlux.collectList())
            .map(tuple -> {
                long totalCount = tuple.getT1();
                List<UserActivityLog> content = tuple.getT2();

                ActivityLogPage page = new ActivityLogPage();
                page.setContent(content);
                page.setTotalElements(totalCount);
                page.setPageNumber(currentPage);
                page.setPageSize(pageSize);

                int totalPages = (int) Math.ceil((double) totalCount / pageSize);
                page.setTotalPages(totalPages);

                return page;
            });
    }

    /**
     * 특정 사용자의 최신 활동 이력 조회
     *
     * @param userEmail 사용자 이메일
     * @param limit     조회할 최대 개수
     * @return 최신 활동 이력 목록
     */
    @Override
    public Mono<List<UserActivityLog>> getRecentActivities(String userEmail, int limit) {
        return userActivityLogDao.findRecentByUserEmail(userEmail, limit)
                                  .collectList();
    }
}

