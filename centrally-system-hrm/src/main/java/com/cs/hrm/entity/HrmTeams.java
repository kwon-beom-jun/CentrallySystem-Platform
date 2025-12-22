package com.cs.hrm.entity;

import com.cs.core.entity.AuditTimeEntity;
import com.cs.core.entity.SoftDeleteEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
	    name = "hrm_teams",
	    uniqueConstraints = {
            /* (부서 + 팀명) 활성행들만 유니크 */
            @UniqueConstraint(
                name = "uk_department_team_enabled",
                columnNames = {"department_id", "team_name", "enabled"}
            )
	    }
	)
@SQLDelete(sql = "UPDATE hrm_teams SET enabled = false, deleted_at = now() WHERE team_id = ?")
public class HrmTeams extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    @Comment("팀 ID")
    private Integer teamId;

//    @Column(name = "team_name", nullable = false, unique = true)
    @Column(name = "team_name")
    @Comment("팀명")
    private String teamName;

    /**
     * 🔥 팀 - 부서 (N:1)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    @Comment("부서 ID")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "teams"})
    // ↑ department에 있는 'teams' 리스트를 무시
    private HrmDepartments department;

    /**
     * (선택) 1팀 - n유저 : 양방향 매핑
     *  → 사용자 조회 시 team → user → team 재귀를 피하기 위해
     *    @JsonIgnoreProperties / @JsonBackReference 등을 적절히 사용
     */
    @OneToMany(mappedBy = "team", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"team"})
    private List<HrmUser> users;
}
