package com.cs.hrm.controller.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 즐겨찾기 메뉴 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteMenuResponse {
    private Long favoriteId;
    private Integer userId;
    private String menuPath;
    private String menuLabel;
    private String menuIcon;
    private String workspace;
    private String category;
    private Integer displayOrder;
    private String color;
    private LocalDateTime createdAt;
}

