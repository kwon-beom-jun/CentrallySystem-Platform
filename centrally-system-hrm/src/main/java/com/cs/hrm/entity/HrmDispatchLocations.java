package com.cs.hrm.entity;

import com.cs.core.entity.AuditTimeEntity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "hrm_dispatch_locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class HrmDispatchLocations extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dispatch_locations_id")
    @Comment("파견 위치 ID")
    private Integer dispatchLocationsId;

    @Column(name = "dispatch_locations_name")
    @Comment("파견 위치 이름")
    private String dispatchLocationsName;
}
