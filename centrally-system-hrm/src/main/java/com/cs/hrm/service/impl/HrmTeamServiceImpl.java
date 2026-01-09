package com.cs.hrm.service.impl;

import com.cs.core.handler.GlobalExceptionHandler;
import com.cs.hrm.entity.HrmTeams;
import com.cs.hrm.repository.HrmTeamsRepository;
import com.cs.hrm.repository.HrmUserRepository;
import com.cs.hrm.service.HrmTeamService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HrmTeamServiceImpl implements HrmTeamService {

    private final HrmTeamsRepository teamRepository;
    private final HrmUserRepository userRepository;
    private final EntityManager em;

    /** 전체 팀 정보 조회 (물리) */
    @Override
    @Transactional(readOnly = true)
    public List<HrmTeams> getTeams() {
        return teamRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public HrmTeams getTeam(Integer teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException(GlobalExceptionHandler.CC + "팀 정보를 찾을 수 없습니다. ID=" + teamId));
    }

    /** 팀 삭제 */
    @Override
    @Transactional
    public void deleteTeam(Integer teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new RuntimeException(
                GlobalExceptionHandler.CC + "팀 정보를 찾을 수 없습니다. ID=" + teamId);
        }

        /* 1) 팀을 참조하던 사용자 FK NULL 처리  */
        userRepository.detachTeam(teamId);   // @Modifying(clearAutomatically=true)

        /* 2) 즉시 DB 반영 (delete 보다 먼저) */
        em.flush();                    // ＊detachTeam 의 UPDATE 가 먼저 나가게

        /* 3) 팀 soft-delete (enabled = false)  */
        teamRepository.deleteById(teamId);
    }
}

