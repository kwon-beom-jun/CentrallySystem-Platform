package com.cs.info.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cs.info.entity.InfoTeams;

/**
 * INFO 팀 레포지토리
 */
@Repository
public interface InfoTeamsRepository extends JpaRepository<InfoTeams, Integer> {
}

