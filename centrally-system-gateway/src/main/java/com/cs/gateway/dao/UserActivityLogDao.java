package com.cs.gateway.dao;

import com.cs.gateway.entity.UserActivityLog;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * 초기 spec 생성 오류
 *	코드 작성 초기에 template.getDatabaseClient().sql("")처럼 빈 문자열로 spec을 생성하면, SQL문이 빈 상태로 전달되어
 *	SQL must not be null or empty 에러가 발생
 *	따라서 SQL문을 바로 생성하고, 조건에 따라 동적으로 SQL을 추가한 후에 그 결과로 spec을 생성하도록 수정
 */
@Repository
public class UserActivityLogDao {

    private final R2dbcEntityTemplate template;
    
    public UserActivityLogDao(R2dbcEntityTemplate template) {
        this.template = template;
    }

    /**
     * 전체 개수 조회 (페이징 처리를 위한 토탈 카운트)
     *
     * @param startDate  (yyyy-MM-dd 형식, 옵션) 시작 날짜
     * @param endDate    (yyyy-MM-dd 형식, 옵션) 종료 날짜
     * @param searchData (옵션) 검색어 (대소문자 구분없이 username, user_email, menu, function, http_method, http_body에 대해 검색)
     * @return 전체 로그 개수를 Mono<Long>로 반환
     */
    public Mono<Long> count(String startDate, String endDate, String searchData) {
        // SQL 동적 생성: 테이블 이름은 스키마 접두어 없이 사용(데이터베이스 설정에 맞게)
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS cnt FROM gateway_user_activity_log WHERE 1=1");

        // 날짜 파싱 (yyyy-MM-dd 형식)
        Optional<LocalDate> startOpt = parseDate(startDate);
        Optional<LocalDate> endOpt   = parseDate(endDate);

        // 날짜 조건 추가
        if (startOpt.isPresent()) {
            sql.append(" AND timestamp >= :startTime");
        }
        if (endOpt.isPresent()) {
            // endDate +1일의 00:00시 (미포함)
            sql.append(" AND timestamp < :endTime");
        }
        // 검색어 조건 추가 (대소문자 구분없이 LIKE 검색)
        if (searchData != null && !searchData.isEmpty()) {
            sql.append(" AND (");
            sql.append("LOWER(username) LIKE :search OR ");
            sql.append("LOWER(user_email) LIKE :search OR ");
            sql.append("LOWER(menu) LIKE :search OR ");
            sql.append("LOWER(function) LIKE :search OR ");
            sql.append("LOWER(http_method) LIKE :search OR ");
            sql.append("LOWER(http_body) LIKE :search");
            sql.append(")");
        }

        // 생성한 SQL문을 이용하여 spec을 생성
        DatabaseClient.GenericExecuteSpec spec = template.getDatabaseClient().sql(sql.toString());

        // 파라미터 바인딩
        if (startOpt.isPresent()) {
            spec = spec.bind("startTime", startOpt.get().atStartOfDay());
        }
        if (endOpt.isPresent()) {
            spec = spec.bind("endTime", endOpt.get().plusDays(1).atStartOfDay());
        }
        if (searchData != null && !searchData.isEmpty()) {
            spec = spec.bind("search", "%" + searchData.toLowerCase() + "%");
        }

        // SQL 실행 후, 결과 매핑하여 개수를 반환 (없으면 0 반환)
        return spec
            .map((row, metadata) -> row.get("cnt", Long.class))
            .one()
            .defaultIfEmpty(0L);
    }

    /**
     * 페이지 데이터 조회 (LIMIT/OFFSET 적용)
     *
     * @param startDate  (yyyy-MM-dd 형식, 옵션) 시작 날짜
     * @param endDate    (yyyy-MM-dd 형식, 옵션) 종료 날짜
     * @param searchData (옵션) 검색어
     * @param pageNumber 페이지 번호 (1부터 시작)
     * @param pageSize   페이지 크기
     * @return 해당 페이지의 로그 데이터를 Flux<UserActivityLog>로 반환
     */
    public Flux<UserActivityLog> findPage(String startDate,
                                          String endDate,
                                          String searchData,
                                          int pageNumber,
                                          int pageSize) {

        // SQL 동적 생성 (WHERE 1=1은 항상 참인 조건으로 시작)
        StringBuilder sql = new StringBuilder("SELECT * FROM gateway_user_activity_log WHERE 1=1");

        Optional<LocalDate> startOpt = parseDate(startDate);
        Optional<LocalDate> endOpt   = parseDate(endDate);

        if (startOpt.isPresent()) {
            sql.append(" AND timestamp >= :startTime");
        }
        if (endOpt.isPresent()) {
            sql.append(" AND timestamp < :endTime");
        }
        if (searchData != null && !searchData.isEmpty()) {
            sql.append(" AND (");
            sql.append("LOWER(username) LIKE :search OR ");
            sql.append("LOWER(user_email) LIKE :search OR ");
            sql.append("LOWER(menu) LIKE :search OR ");
            sql.append("LOWER(function) LIKE :search OR ");
            sql.append("LOWER(http_method) LIKE :search OR ");
            sql.append("LOWER(http_body) LIKE :search");
            sql.append(")");
        }

        // 정렬(최근 로그가 먼저 나오도록) 및 페이징(OFFSET, LIMIT)
        sql.append(" ORDER BY timestamp DESC");
        sql.append(" LIMIT :limit OFFSET :offset");

        // SQL문과 함께 spec 생성
        DatabaseClient.GenericExecuteSpec spec = template.getDatabaseClient().sql(sql.toString());

        // 파라미터 바인딩
        if (startOpt.isPresent()) {
            spec = spec.bind("startTime", startOpt.get().atStartOfDay());
        }
        if (endOpt.isPresent()) {
            spec = spec.bind("endTime", endOpt.get().plusDays(1).atStartOfDay());
        }
        if (searchData != null && !searchData.isEmpty()) {
            spec = spec.bind("search", "%" + searchData.toLowerCase() + "%");
        }
        int offset = Math.max(pageNumber - 1, 0) * pageSize;
        spec = spec.bind("limit", pageSize)
                   .bind("offset", offset);

        // 결과 매핑: R2dbcEntityTemplate의 Converter를 사용하여 UserActivityLog 객체로 변환
        return spec
            .map((row, meta) -> template.getConverter().read(UserActivityLog.class, row))
            .all();
    }

    /**
     * 로그 저장
     *
     * @param entity 저장할 UserActivityLog 엔티티
     * @return 저장된 엔티티를 Mono<UserActivityLog>로 반환
     */
    public Mono<UserActivityLog> saveLog(UserActivityLog entity) {
        return template.insert(entity);
    }

    /**
     * 특정 사용자의 개인 활동 이력 개수 조회 (페이징 처리용)
     *
     * @param userEmail  사용자 이메일
     * @param startDate  (yyyy-MM-dd 형식, 옵션) 시작 날짜
     * @param endDate    (yyyy-MM-dd 형식, 옵션) 종료 날짜
     * @param searchData (옵션) 검색어
     * @return 전체 로그 개수를 Mono<Long>로 반환
     */
    public Mono<Long> countPersonal(String userEmail, String startDate, String endDate, String searchData) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS cnt FROM gateway_user_activity_log WHERE user_email = :userEmail");

        // 날짜 파싱
        Optional<LocalDate> startOpt = parseDate(startDate);
        Optional<LocalDate> endOpt   = parseDate(endDate);

        // 날짜 조건 추가
        if (startOpt.isPresent()) {
            sql.append(" AND timestamp >= :startTime");
        }
        if (endOpt.isPresent()) {
            sql.append(" AND timestamp < :endTime");
        }
        // 검색어 조건 추가
        if (searchData != null && !searchData.isEmpty()) {
            sql.append(" AND (");
            sql.append("LOWER(menu) LIKE :search OR ");
            sql.append("LOWER(function) LIKE :search OR ");
            sql.append("LOWER(http_method) LIKE :search OR ");
            sql.append("LOWER(http_body) LIKE :search");
            sql.append(")");
        }

        DatabaseClient.GenericExecuteSpec spec = template.getDatabaseClient()
            .sql(sql.toString())
            .bind("userEmail", userEmail);

        // 파라미터 바인딩
        if (startOpt.isPresent()) {
            spec = spec.bind("startTime", startOpt.get().atStartOfDay());
        }
        if (endOpt.isPresent()) {
            spec = spec.bind("endTime", endOpt.get().plusDays(1).atStartOfDay());
        }
        if (searchData != null && !searchData.isEmpty()) {
            spec = spec.bind("search", "%" + searchData.toLowerCase() + "%");
        }

        return spec
            .map((row, metadata) -> row.get("cnt", Long.class))
            .one()
            .defaultIfEmpty(0L);
    }

    /**
     * 특정 사용자의 개인 활동 이력 페이지 데이터 조회
     *
     * @param userEmail  사용자 이메일
     * @param startDate  (yyyy-MM-dd 형식, 옵션) 시작 날짜
     * @param endDate    (yyyy-MM-dd 형식, 옵션) 종료 날짜
     * @param searchData (옵션) 검색어
     * @param pageNumber 페이지 번호 (1부터 시작)
     * @param pageSize   페이지 크기
     * @return 해당 페이지의 로그 데이터를 Flux<UserActivityLog>로 반환
     */
    public Flux<UserActivityLog> findPersonalPage(String userEmail,
                                                  String startDate,
                                                  String endDate,
                                                  String searchData,
                                                  int pageNumber,
                                                  int pageSize) {
        StringBuilder sql = new StringBuilder("SELECT * FROM gateway_user_activity_log WHERE user_email = :userEmail");

        Optional<LocalDate> startOpt = parseDate(startDate);
        Optional<LocalDate> endOpt   = parseDate(endDate);

        if (startOpt.isPresent()) {
            sql.append(" AND timestamp >= :startTime");
        }
        if (endOpt.isPresent()) {
            sql.append(" AND timestamp < :endTime");
        }
        if (searchData != null && !searchData.isEmpty()) {
            sql.append(" AND (");
            sql.append("LOWER(menu) LIKE :search OR ");
            sql.append("LOWER(function) LIKE :search OR ");
            sql.append("LOWER(http_method) LIKE :search OR ");
            sql.append("LOWER(http_body) LIKE :search");
            sql.append(")");
        }

        sql.append(" ORDER BY timestamp DESC");
        sql.append(" LIMIT :limit OFFSET :offset");

        DatabaseClient.GenericExecuteSpec spec = template.getDatabaseClient()
            .sql(sql.toString())
            .bind("userEmail", userEmail);

        // 파라미터 바인딩
        if (startOpt.isPresent()) {
            spec = spec.bind("startTime", startOpt.get().atStartOfDay());
        }
        if (endOpt.isPresent()) {
            spec = spec.bind("endTime", endOpt.get().plusDays(1).atStartOfDay());
        }
        if (searchData != null && !searchData.isEmpty()) {
            spec = spec.bind("search", "%" + searchData.toLowerCase() + "%");
        }
        int offset = Math.max(pageNumber - 1, 0) * pageSize;
        spec = spec.bind("limit", pageSize)
                   .bind("offset", offset);

        return spec
            .map((row, meta) -> template.getConverter().read(UserActivityLog.class, row))
            .all();
    }

    /**
     * 특정 사용자의 최신 활동 이력 조회
     *
     * @param userEmail 사용자 이메일
     * @param limit     조회할 최대 개수
     * @return 최신 활동 이력 목록을 Flux<UserActivityLog>로 반환
     */
    public Flux<UserActivityLog> findRecentByUserEmail(String userEmail, int limit) {
        String sql = "SELECT * FROM gateway_user_activity_log WHERE user_email = :userEmail ORDER BY timestamp DESC LIMIT :limit";
        
        DatabaseClient.GenericExecuteSpec spec = template.getDatabaseClient()
            .sql(sql)
            .bind("userEmail", userEmail)
            .bind("limit", limit);
        
        return spec
            .map((row, meta) -> template.getConverter().read(UserActivityLog.class, row))
            .all();
    }

    /**
     * yyyy-MM-dd 형식 파싱
     *
     * @param str 날짜 문자열 (예: "2025-03-04")
     * @return 파싱 성공 시 Optional.of(LocalDate), 실패 또는 null/빈 문자열이면 Optional.empty()
     */
    private Optional<LocalDate> parseDate(String str) {
        if (str == null || str.isEmpty()) return Optional.empty();
        try {
            // yyyy-MM-dd 형식으로 파싱
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate date = LocalDate.parse(str, formatter);
            return Optional.of(date);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}