package com.cs.rcpt.service;

/**
 * 영수증 HRM 동기화 서비스 인터페이스
 */
public interface ReceiptHrmSyncService {

    /**
     * 부서명 변경 동기화
     * @param deptId 부서 ID
     * @param newName 새 부서명
     * @return 업데이트된 영수증 수
     */
    int renameDepartment(Integer deptId, String newName);

    /**
     * 부서 삭제 동기화
     * @param deptId 부서 ID
     * @return 업데이트된 영수증 수
     */
    int deleteDepartment(Integer deptId);

    /**
     * 팀명 변경 동기화
     * @param teamId 팀 ID
     * @param newName 새 팀명
     * @return 업데이트된 영수증 수
     */
    int renameTeam(Integer teamId, String newName);

    /**
     * 팀 삭제 동기화
     * @param teamId 팀 ID
     * @return 업데이트된 영수증 수
     */
    int deleteTeam(Integer teamId);

    /**
     * 사용자 부서/팀 동기화
     * @param userId 사용자 ID
     * @param deptId 부서 ID
     * @param deptName 부서명
     * @param teamId 팀 ID
     * @param teamName 팀명
     * @return 업데이트된 영수증 수
     */
    int updateUserDeptTeam(Integer userId,
                          Integer deptId, String deptName,
                          Integer teamId,  String teamName);
}
