package com.cs.hrm.service;

import com.cs.hrm.controller.request.FavoriteMenuRequest;
import com.cs.hrm.controller.response.FavoriteMenuResponse;
import com.cs.hrm.entity.HrmUserFavoriteMenu;

import java.util.List;

/**
 * 사용자 즐겨찾기 메뉴 서비스 인터페이스
 */
public interface HrmUserFavoriteMenuService {

    /**
     * 사용자의 즐겨찾기 목록 조회
     * @param userId 사용자 ID
     * @return 즐겨찾기 목록
     */
    List<FavoriteMenuResponse> getFavoriteMenus(Integer userId);

    /**
     * 즐겨찾기 생성
     * @param userId 사용자 ID
     * @param request 즐겨찾기 요청
     * @return 생성된 즐겨찾기 응답
     */
    FavoriteMenuResponse createFavoriteMenu(Integer userId, FavoriteMenuRequest request);

    /**
     * 즐겨찾기 삭제
     * @param userId 사용자 ID
     * @param menuPath 메뉴 경로
     */
    void deleteFavoriteMenu(Integer userId, String menuPath);

    /**
     * 사용자의 모든 즐겨찾기 삭제
     * @param userId 사용자 ID
     */
    void clearFavoriteMenus(Integer userId);

    /**
     * 즐겨찾기 여부 확인
     * @param userId 사용자 ID
     * @param menuPath 메뉴 경로
     * @return 즐겨찾기 여부
     */
    boolean isFavorite(Integer userId, String menuPath);

    /**
     * 즐겨찾기 순서 변경
     * @param userId 사용자 ID
     * @param favoriteIds 즐겨찾기 ID 목록
     */
    void reorderFavoriteMenus(Integer userId, List<Long> favoriteIds);

    /**
     * 즐겨찾기 색상 변경
     * @param userId 사용자 ID
     * @param favoriteId 즐겨찾기 ID
     * @param color 색상
     */
    void updateFavoriteMenuColor(Integer userId, Long favoriteId, String color);
}
