package com.cs.hrm.entity;

import com.cs.core.entity.AuditTimeEntity;
import com.cs.core.entity.SoftDeleteEntity;
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
	    name = "hrm_departments",
	    /* department_name + enabled 조합만 UNIQUE */
	    uniqueConstraints = {
	        @UniqueConstraint(
	            name = "uk_dept_name_enabled",
	            columnNames = {"department_name", "enabled"}
	        )
	    }
	)
@SQLDelete(sql = "UPDATE hrm_departments SET enabled = false, deleted_at = now() WHERE department_id = ?")
public class HrmDepartments extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    @Comment("부서 ID")
    private Integer departmentId;

    @Column(name = "department_name", nullable = false)
    @Comment("부서 이름")
    private String departmentName;

    /**
     * (선택) 1부서 - n팀
     */
    @OneToMany(mappedBy = "department",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Where(clause = "enabled = true")
    @JsonIgnoreProperties({"department"}) 
    // ↑ 팀 안에서 다시 department를 거슬러 올라가는 무한루프 방지
    private List<HrmTeams> teams;
}
