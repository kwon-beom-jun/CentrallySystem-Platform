package com.cs.hrm.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import com.cs.core.entity.AuditTimeEntity;

/**
 * 사용자 즐겨찾기 메뉴
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "hrm_user_favorite_menus",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "menu_path"})
    },
    indexes = {
        @Index(name = "idx_hrm_user_favorite_menus_user_id", columnList = "user_id"),
        @Index(name = "idx_hrm_user_favorite_menus_menu_path", columnList = "menu_path")
    }
)
public class HrmUserFavoriteMenu extends AuditTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    @Comment("즐겨찾기 ID")
    private Long favoriteId;

    @Column(name = "user_id", nullable = false)
    @Comment("사용자 ID")
    private Integer userId;

    @Column(name = "menu_path", nullable = false, length = 255)
    @Comment("메뉴 경로")
    private String menuPath;

    @Column(name = "menu_label", nullable = false, length = 100)
    @Comment("메뉴 라벨")
    private String menuLabel;

    @Column(name = "menu_icon", length = 100)
    @Comment("메뉴 아이콘")
    private String menuIcon;

    @Column(name = "workspace", length = 50)
    @Comment("워크스페이스")
    private String workspace;

    @Column(name = "category", length = 50)
    @Comment("카테고리")
    private String category;

    @Column(name = "display_order")
    @Comment("정렬 순서")
    private Integer displayOrder;

    @Column(name = "color", length = 20)
    @Comment("칩 색상")
    private String color;

}

