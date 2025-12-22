package com.cs.info.entity;

import com.cs.core.entity.SoftDeleteEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Comment;

/**
 * INFO 팀 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "info_teams",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_department_team_enabled",
            columnNames = {"department_id", "team_name", "enabled"}
        )
    }
)
@SQLDelete(sql = "UPDATE info_teams SET enabled = false, deleted_at = now() WHERE team_id = ?")
public class InfoTeams extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    @Comment("팀 ID")
    private Integer teamId;

    @Column(name = "team_name")
    @Comment("팀명")
    private String teamName;

    /**
     * 팀 - 부서 (N:1)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    @Comment("부서 ID")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "teams"})
    private InfoDepartments department;
}

