package com.cs.hrm.repository;

import com.cs.hrm.entity.HrmUserFavoriteMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 즐겨찾기 메뉴 Repository
 */
@Repository
public interface HrmUserFavoriteMenuRepository extends JpaRepository<HrmUserFavoriteMenu, Long> {

    /**
     * 사용자 ID로 즐겨찾기 목록 조회
     */
    List<HrmUserFavoriteMenu> findByUserIdOrderByDisplayOrderAsc(Integer userId);

    /**
     * 사용자 ID와 메뉴 경로로 즐겨찾기 조회
     */
    Optional<HrmUserFavoriteMenu> findByUserIdAndMenuPath(Integer userId, String menuPath);

    /**
     * 사용자 ID와 메뉴 경로로 즐겨찾기 삭제
     */
    void deleteByUserIdAndMenuPath(Integer userId, String menuPath);

    /**
     * 사용자 ID로 모든 즐겨찾기 삭제
     */
    void deleteByUserId(Integer userId);

    /**
     * 사용자 ID와 메뉴 경로로 존재 여부 확인
     */
    boolean existsByUserIdAndMenuPath(Integer userId, String menuPath);
}

