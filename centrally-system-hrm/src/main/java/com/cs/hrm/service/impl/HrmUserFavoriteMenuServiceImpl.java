package com.cs.hrm.service.impl;

import com.cs.hrm.controller.request.FavoriteMenuRequest;
import com.cs.hrm.controller.response.FavoriteMenuResponse;
import com.cs.hrm.entity.HrmUserFavoriteMenu;
import com.cs.hrm.repository.HrmUserFavoriteMenuRepository;
import com.cs.hrm.service.HrmUserFavoriteMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 즐겨찾기 메뉴 서비스 구현체
 */
@Service
@RequiredArgsConstructor
public class HrmUserFavoriteMenuServiceImpl implements HrmUserFavoriteMenuService {

    private final HrmUserFavoriteMenuRepository favoriteMenuRepository;

    /**
     * 사용자의 즐겨찾기 목록 조회
     */
    @Override
    @Transactional(readOnly = true)
    public List<FavoriteMenuResponse> getFavoriteMenus(Integer userId) {
        List<HrmUserFavoriteMenu> favorites = favoriteMenuRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        return favorites.stream()
                        .map(this::toResponse)
                        .collect(Collectors.toList());
    }

    /**
     * 즐겨찾기 생성
     */
    @Override
    @Transactional
    public FavoriteMenuResponse createFavoriteMenu(Integer userId, FavoriteMenuRequest request) {
        // 중복 체크
        if (favoriteMenuRepository.existsByUserIdAndMenuPath(userId, request.getMenuPath())) {
            throw new IllegalArgumentException("이미 즐겨찾기에 추가된 메뉴입니다.");
        }

        // 현재 최대 order 값 조회
        List<HrmUserFavoriteMenu> existingFavorites = favoriteMenuRepository.findByUserIdOrderByDisplayOrderAsc(userId);
        Integer maxOrder = existingFavorites.stream()
                                           .map(HrmUserFavoriteMenu::getDisplayOrder)
                                           .filter(order -> order != null)
                                           .max(Integer::compare)
                                           .orElse(0);

        HrmUserFavoriteMenu favorite = HrmUserFavoriteMenu.builder()
                                                          .userId(userId)
                                                          .menuPath(request.getMenuPath())
                                                          .menuLabel(request.getMenuLabel())
                                                          .menuIcon(request.getMenuIcon())
                                                          .workspace(request.getWorkspace())
                                                          .category(request.getCategory())
                                                          .displayOrder(maxOrder + 1)
                                                          .color("gray")
                                                          .build();

        HrmUserFavoriteMenu saved = favoriteMenuRepository.save(favorite);
        return toResponse(saved);
    }

    /**
     * 즐겨찾기 삭제
     */
    @Override
    @Transactional
    public void deleteFavoriteMenu(Integer userId, String menuPath) {
        favoriteMenuRepository.deleteByUserIdAndMenuPath(userId, menuPath);
    }

    /**
     * 사용자의 모든 즐겨찾기 삭제
     */
    @Override
    @Transactional
    public void clearFavoriteMenus(Integer userId) {
        favoriteMenuRepository.deleteByUserId(userId);
    }

    /**
     * 즐겨찾기 여부 확인
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(Integer userId, String menuPath) {
        return favoriteMenuRepository.existsByUserIdAndMenuPath(userId, menuPath);
    }

    /**
     * 즐겨찾기 순서 변경
     */
    @Override
    @Transactional
    public void reorderFavoriteMenus(Integer userId, List<Long> favoriteIds) {
        for (int i = 0; i < favoriteIds.size(); i++) {
            final int order = i + 1;
            Long favoriteId = favoriteIds.get(i);
            favoriteMenuRepository.findById(favoriteId).ifPresent(favorite -> {
                if (favorite.getUserId().equals(userId)) {
                    favorite.setDisplayOrder(order);
                    favoriteMenuRepository.save(favorite);
                }
            });
        }
    }

    /**
     * 즐겨찾기 색상 변경
     */
    @Override
    @Transactional
    public void updateFavoriteMenuColor(Integer userId, Long favoriteId, String color) {
        favoriteMenuRepository.findById(favoriteId).ifPresent(favorite -> {
            if (favorite.getUserId().equals(userId)) {
                favorite.setColor(color);
                favoriteMenuRepository.save(favorite);
            }
        });
    }

    /**
     * Entity to Response DTO
     */
    private FavoriteMenuResponse toResponse(HrmUserFavoriteMenu entity) {
        return FavoriteMenuResponse.builder()
                                   .favoriteId(entity.getFavoriteId())
                                   .userId(entity.getUserId())
                                   .menuPath(entity.getMenuPath())
                                   .menuLabel(entity.getMenuLabel())
                                   .menuIcon(entity.getMenuIcon())
                                   .workspace(entity.getWorkspace())
                                   .category(entity.getCategory())
                                   .displayOrder(entity.getDisplayOrder())
                                   .color(entity.getColor())
                                   .createdAt(entity.getRegTime())
                                   .build();
    }
}

