package com.cs.hrm.controller.response;

import com.cs.hrm.entity.HrmTeams;
import lombok.Builder;
import lombok.Getter;

@Getter
public class TeamResponse {
    private final Integer teamId;
    private final String teamName;

    @Builder
    public TeamResponse(Integer teamId, String teamName) {
        this.teamId = teamId;
        this.teamName = teamName;
    }

    // HrmTeams 엔티티를 TeamResponse DTO로 변환하는 정적 팩토리 메서드
    public static TeamResponse from(HrmTeams team) {
        return TeamResponse.builder()
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .build();
    }
}