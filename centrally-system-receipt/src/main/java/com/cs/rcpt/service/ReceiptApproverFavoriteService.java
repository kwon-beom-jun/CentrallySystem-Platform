package com.cs.rcpt.service;

import com.cs.rcpt.controller.request.ApproverFavoriteDto;

import java.util.List;

/**
 * 영수증 결재자 즐겨찾기 서비스 인터페이스
 */
public interface ReceiptApproverFavoriteService {

    /**
     * 즐겨찾기 목록 조회
     * @param ownerUserId 소유자 사용자 ID
     * @return 즐겨찾기 목록
     */
    List<ApproverFavoriteDto> getApproverFavorites(Long ownerUserId);

    /**
     * 즐겨찾기 생성
     * @param dto 즐겨찾기 DTO
     * @return 생성된 즐겨찾기 DTO
     */
    ApproverFavoriteDto createApproverFavorite(ApproverFavoriteDto dto);

    /**
     * 즐겨찾기 삭제
     * @param ownerUserId 소유자 사용자 ID
     * @param favoriteUserId 즐겨찾기 사용자 ID
     */
    void deleteApproverFavorite(Long ownerUserId, Long favoriteUserId);

    /**
     * 즐겨찾기 순서 변경
     * @param ownerUserId 소유자 사용자 ID
     * @param orderList 순서 목록
     */
    void udpateApproverFavorite(Long ownerUserId, List<ApproverFavoriteDto> orderList);

    /**
     * 권한 상실 사용자에 대한 즐겨찾기 전체 제거
     * @param favoriteUserId 즐겨찾기 사용자 ID
     */
    void deleteAllByFavoriteUser(Long favoriteUserId);
}
