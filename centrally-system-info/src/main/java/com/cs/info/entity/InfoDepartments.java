package com.cs.info.entity;

import com.cs.core.entity.SoftDeleteEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.Comment;

/**
 * INFO 부서 엔티티
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "info_departments",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_dept_name_enabled",
            columnNames = {"department_name", "enabled"}
        )
    }
)
@SQLDelete(sql = "UPDATE info_departments SET enabled = false, deleted_at = now() WHERE department_id = ?")
public class InfoDepartments extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    @Comment("부서 ID")
    private Integer departmentId;

    @Column(name = "department_name", nullable = false)
    @Comment("부서 이름")
    private String departmentName;

    /**
     * 1부서 - n팀
     */
    @OneToMany(mappedBy = "department",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Where(clause = "enabled = true")
    @JsonIgnoreProperties({"department"})
    private List<InfoTeams> teams;
}

