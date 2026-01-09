package com.cs.rcpt.service;

import com.cs.rcpt.controller.request.ReceiptDeptTeamUpdate;
import com.cs.rcpt.controller.request.ReceiptDecisionRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * 영수증 신청/결재 서비스 인터페이스
 */
public interface ReceiptRequestService {

    /**
     * [단건] 신청자가 자신의 영수증을 'REQUEST' 상태로 전환
     * - WAITING → REQUEST  |  REJECTED → REQUEST(재신청)
     * - 결재선에 '결재(approvalRole=1)'가 최소 1명 있어야 함
     * @param receiptId 영수증 PK
     * @param userId 로그인 사용자 PK (= 작성자)
     * @param requestDto 부서/팀 업데이트 정보
     */
    void requestReceipt(Integer receiptId, Integer userId, ReceiptDeptTeamUpdate requestDto);

    /**
     * [단건] (한 신청자) 다건 영수증 승인/반려 저장
     * - ReceiptDecisionRequest 리스트 기반
     * - currentApprovalStep 자동 이동 & 상태 전환
     * @param userId 신청자 PK
     * @param decisions 결재 결과 목록
     */
    void saveReceiptDecisions(Integer userId, List<ReceiptDecisionRequest> decisions);

    /**
     * [일괄 마감] 영수증 CLOSED 전환
     * @param receiptIds 마감 대상 PK 목록
     */
    void closeReceipts(List<Integer> receiptIds);

    /**
     * 여러 영수증을 '반려(REJECTED)' 상태로 일괄 변경
     * @param receiptIds 반려할 영수증 ID 목록
     * @param reason 반려 사유
     * @param changerId 변경자 ID
     * @param changerName 변경자 이름
     */
    void rejectReceipts(List<Integer> receiptIds, String reason, Integer changerId, String changerName);

    /**
     * 사용자 비활성화에 따른 영수증 후속 처리를 수행
     * - 영수증 상태가 'REQUEST' & '해당 사용자가 승인 상태'면 결재선에서 사용자만 제거하고 재정렬합니다.
     * - 영수증 상태가 'REQUEST' & '해당 사용자가 승인 전'이라면 'REJECTED'로 변경하고 사유를 기록 및 재정렬합니다.
     * - 영수증 상태가 'REJECTED' 또는 'WAITING'이면 결재선에서 사용자만 제거하고 재정렬합니다.
     * @param receiptIds 영수증 ID 목록
     * @param userToRemoveId 제거할 사용자 ID
     * @param reason 사유
     * @param changerId 변경자 ID
     * @param changerName 변경자 이름
     */
    void rejectAndRemoveApprover(List<Integer> receiptIds, Integer userToRemoveId, String reason, Integer changerId, String changerName);

    /**
     * [일괄 승인] (결재자 기준) '내 차례' 영수증 다건 승인
     * @param approverId 결재자 PK
     * @param approverName 결재자 이름
     * @param userIds 신청자 PK 배열
     * @param start 시작 날짜
     * @param end 종료 날짜
     * @param roles 역할 목록
     */
    void bulkApprove(Integer approverId, String approverName, List<Integer> userIds, LocalDate start, LocalDate end, List<Integer> roles);

    /**
     * [일괄 반려] (결재자 기준) '내 차례' 영수증 다건 반려
     * @param approverId 결재자 PK
     * @param approverName 결재자 이름
     * @param userIds 신청자 PK 배열
     * @param start 시작 날짜
     * @param end 종료 날짜
     * @param reason 반려 사유
     * @param roles 역할 목록
     */
    void bulkReject(Integer approverId, String approverName, List<Integer> userIds, LocalDate start, LocalDate end, String reason, List<Integer> roles);

    /**
     * [강제 변경] 관리자용 상태/결재선 리셋
     * @param receiptId 영수증 ID
     * @param statusCode 상태 코드
     * @param changerId 변경자 ID
     * @param changerName 변경자 이름
     * @param requestDto 부서/팀 업데이트 정보
     */
    void forceChangeStatus(Integer receiptId, String statusCode, Integer changerId, String changerName, ReceiptDeptTeamUpdate requestDto);
}
