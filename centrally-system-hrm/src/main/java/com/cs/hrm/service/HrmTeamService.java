package com.cs.hrm.service;

import com.cs.hrm.entity.HrmTeams;

import java.util.List;

/**
 * 팀 서비스 인터페이스
 */
public interface HrmTeamService {

    /**
     * 전체 팀 정보 조회 (물리)
     * @return 팀 목록
     */
    List<HrmTeams> getTeams();

    /**
     * ID로 팀 조회
     * @param teamId 팀 ID
     * @return 팀 엔티티
     */
    HrmTeams getTeam(Integer teamId);

    /**
     * 팀 삭제
     * @param teamId 팀 ID
     */
    void deleteTeam(Integer teamId);
}
