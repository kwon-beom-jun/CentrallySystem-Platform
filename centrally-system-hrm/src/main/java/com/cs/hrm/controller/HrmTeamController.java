package com.cs.hrm.controller;

import com.cs.hrm.entity.HrmTeams;
import com.cs.hrm.service.HrmTeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
public class HrmTeamController {

    private final HrmTeamService teamService;

    public HrmTeamController(HrmTeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * 전체 팀 정보를 조회하는 API 엔드포인트입니다.
     *
     * @return ResponseEntity로 감싼 팀 목록
     */
    @GetMapping
    public ResponseEntity<List<HrmTeams>> getTeams() {
        List<HrmTeams> teams = teamService.getTeams();
        return ResponseEntity.ok(teams);
    }
}
