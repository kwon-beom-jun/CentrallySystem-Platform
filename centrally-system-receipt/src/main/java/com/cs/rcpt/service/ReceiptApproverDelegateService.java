package com.cs.rcpt.service;

import com.cs.rcpt.entity.ReceiptApproverDelegate;

import java.util.List;
import java.util.Optional;

/**
 * 영수증 결재자 대리 서비스 인터페이스
 */
public interface ReceiptApproverDelegateService {

    /**
     * 활성 대리자 조회
     * @param principalUserId 원결재자 사용자 ID
     * @return 대리자 매핑 정보
     */
    Optional<ReceiptApproverDelegate> getActiveDelegate(Integer principalUserId);

    /**
     * 현재 나를 대리자로 지정한 원결재자 목록(여러 명 가능)
     * @param delegateUserId 대리자 사용자 ID
     * @return 원결재자 목록
     */
    List<ReceiptApproverDelegate> getActivePrincipalsByDelegate(Integer delegateUserId);

    /**
     * 대리자 매핑 생성
     * @param mapping 대리자 매핑 엔티티
     * @return 생성된 대리자 매핑 엔티티
     */
    ReceiptApproverDelegate createDelegate(ReceiptApproverDelegate mapping);

    /**
     * 대리자 매핑 삭제
     * @param id 대리자 매핑 ID
     */
    void deleteDelegate(Long id);

    /**
     * 사용자 비활성화 시: 해당 사용자가 원결재자/대리결재자 어느 쪽으로든 얽힌 모든 대리 매핑 소프트 삭제
     * @param userId 사용자 ID
     * @return 삭제된 매핑 수
     */
    int softDeleteAllMappingsByUser(Integer userId);
}
