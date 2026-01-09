package com.cs.info.repository;

import com.cs.info.entity.InfoSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 일정 Repository
 */
@Repository
public interface InfoScheduleRepository extends JpaRepository<InfoSchedule, Long> {

    /**
     * 기간 내 일정 조회 (활성 일정만)
     */
    @Query("SELECT s FROM InfoSchedule s WHERE s.enabled = true " +
           "AND s.startDate <= :endDate AND s.endDate >= :startDate " +
           "ORDER BY s.startDate ASC, s.startTime ASC")
    List<InfoSchedule> findByDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 부서별 일정 조회 (활성 일정만)
     */
    @Query("SELECT s FROM InfoSchedule s WHERE s.enabled = true " +
           "AND s.departmentId = :departmentId " +
           "AND s.startDate <= :endDate AND s.endDate >= :startDate " +
           "ORDER BY s.startDate ASC, s.startTime ASC")
    List<InfoSchedule> findByDepartmentAndDateRange(
        @Param("departmentId") Integer departmentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 담당자별 일정 조회 (활성 일정만)
     */
    @Query("SELECT s FROM InfoSchedule s WHERE s.enabled = true " +
           "AND s.assigneeId = :assigneeId " +
           "AND s.startDate <= :endDate AND s.endDate >= :startDate " +
           "ORDER BY s.startDate ASC, s.startTime ASC")
    List<InfoSchedule> findByAssigneeAndDateRange(
        @Param("assigneeId") Integer assigneeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 작성자별 일정 조회 (활성 일정만)
     */
    @Query("SELECT s FROM InfoSchedule s WHERE s.enabled = true " +
           "AND s.creatorId = :creatorId " +
           "ORDER BY s.startDate DESC")
    List<InfoSchedule> findByCreatorId(@Param("creatorId") Integer creatorId);

    /**
     * 알림 발송 대상 일정 조회
     * - 활성 일정만
     * - 시작 시간이 미래인 일정
     * - 지정된 기간 내에 시작하는 일정
     * - scheduleType을 JOIN FETCH하여 LazyInitializationException 방지
     */
    @Query("SELECT s FROM InfoSchedule s " +
           "JOIN FETCH s.scheduleType " +
           "WHERE s.enabled = true " +
           "AND s.deletedAt IS NULL " +
           "AND (s.startTime IS NOT NULL AND s.startTime > :now " +
           "     OR (s.startTime IS NULL AND s.startDate >= :today)) " +
           "AND s.startDate <= :endDate " +
           "ORDER BY s.startDate ASC, s.startTime ASC")
    List<InfoSchedule> findSchedulesForNotification(
        @Param("now") java.time.LocalDateTime now,
        @Param("today") java.time.LocalDate today,
        @Param("endDate") LocalDate endDate
    );
}

