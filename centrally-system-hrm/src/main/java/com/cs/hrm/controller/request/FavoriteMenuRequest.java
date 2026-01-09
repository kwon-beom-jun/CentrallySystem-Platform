package com.cs.hrm.controller.request;

import lombok.*;

/**
 * 즐겨찾기 메뉴 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteMenuRequest {
    private String menuPath;
    private String menuLabel;
    private String menuIcon;
    private String workspace;
    private String category;
}

