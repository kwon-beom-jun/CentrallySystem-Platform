package com.cs.hrm.controller;

import com.cs.hrm.controller.request.FavoriteMenuRequest;
import com.cs.hrm.controller.response.FavoriteMenuResponse;
import com.cs.hrm.service.HrmUserFavoriteMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 즐겨찾기 메뉴 API
 */
@RestController
@RequestMapping("/users/{userId}/favorite-menus")
@RequiredArgsConstructor
public class HrmUserFavoriteMenuController {

    private final HrmUserFavoriteMenuService favoriteMenuService;

    /**
     * 즐겨찾기 목록 조회
     * GET /users/{userId}/favorite-menus
     */
    @GetMapping
    public ResponseEntity<List<FavoriteMenuResponse>> getFavoriteMenus(
            @PathVariable("userId") Integer userId) {
        List<FavoriteMenuResponse> favorites = favoriteMenuService.getFavoriteMenus(userId);
        return ResponseEntity.ok(favorites);
    }

    /**
     * 즐겨찾기 생성
     * POST /users/{userId}/favorite-menus
     */
    @PostMapping
    public ResponseEntity<FavoriteMenuResponse> createFavoriteMenu(
            @PathVariable("userId") Integer userId,
            @RequestBody FavoriteMenuRequest request) {
        try {
            FavoriteMenuResponse response = favoriteMenuService.createFavoriteMenu(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * 즐겨찾기 삭제
     * DELETE /users/{userId}/favorite-menus
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteFavoriteMenu(
            @PathVariable("userId") Integer userId,
            @RequestParam("menuPath") String menuPath) {
        favoriteMenuService.deleteFavoriteMenu(userId, menuPath);
        return ResponseEntity.noContent().build();
    }

    /**
     * 모든 즐겨찾기 삭제
     * DELETE /users/{userId}/favorite-menus/all
     */
    @DeleteMapping("/all")
    public ResponseEntity<Void> clearFavoriteMenus(
            @PathVariable("userId") Integer userId) {
        favoriteMenuService.clearFavoriteMenus(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 즐겨찾기 여부 확인
     * GET /users/{userId}/favorite-menus/check
     */
    @GetMapping("/check")
    public ResponseEntity<Boolean> isFavorite(
            @PathVariable("userId") Integer userId,
            @RequestParam("menuPath") String menuPath) {
        boolean isFavorite = favoriteMenuService.isFavorite(userId, menuPath);
        return ResponseEntity.ok(isFavorite);
    }

    /**
     * 즐겨찾기 순서 변경
     * PUT /users/{userId}/favorite-menus/reorder
     */
    @PutMapping("/reorder")
    public ResponseEntity<Void> reorderFavoriteMenus(
            @PathVariable("userId") Integer userId,
            @RequestBody List<Long> favoriteIds) {
        favoriteMenuService.reorderFavoriteMenus(userId, favoriteIds);
        return ResponseEntity.ok().build();
    }

    /**
     * 즐겨찾기 색상 변경
     * PATCH /users/{userId}/favorite-menus/{favoriteId}/color
     */
    @PatchMapping("/{favoriteId}/color")
    public ResponseEntity<Void> updateFavoriteMenuColor(
            @PathVariable("userId") Integer userId,
            @PathVariable("favoriteId") Long favoriteId,
            @RequestBody java.util.Map<String, String> request) {
        String color = request.get("color");
        favoriteMenuService.updateFavoriteMenuColor(userId, favoriteId, color);
        return ResponseEntity.ok().build();
    }
}

