package com.cs.rcpt.repository;

import com.cs.rcpt.entity.Receipt;
import com.cs.rcpt.enums.ReceiptProcessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

/**
 * (관리용)
 * 이전과 동일하게 데이터 동기화를 위한 @Modifying 쿼리를 담당합니다.
 */
public interface ReceiptAdminRepository {
    
    
    /** --- HRM 서비스 전달, 부서(팀) 정보 업데이트 ----------------------------- */
    /* 신청 상태(대기/요청)만 가져오기 위한 공통 where */
//    List<Receipt> findByDepartmentIdAndStatusIn(Integer deptId, List<ReceiptProcessStatus> statuses);
//    List<Receipt> findByTeamIdAndStatusIn(Integer teamId, List<ReceiptProcessStatus> statuses);

    @Modifying @Transactional
    @Query("""
        update Receipt r
           set r.departmentName = :newName
         where r.departmentId   = :deptId
           and r.status in :statuses
    """)
    int renameDepartment(@Param("deptId")    Integer deptId,
                         @Param("newName")   String  newName,
                         @Param("statuses")  List<ReceiptProcessStatus> statuses);

    @Modifying @Transactional
    @Query("""
        update Receipt r
           set r.departmentId   = null,
               r.departmentName = null,
               r.teamId         = null,
               r.teamName       = null
         where r.departmentId   = :deptId
           and r.status in :statuses
    """)
    int nullifyDepartment(@Param("deptId")   Integer deptId,
                          @Param("statuses") List<ReceiptProcessStatus> statuses);

    @Modifying @Transactional
    @Query("""
        update Receipt r
           set r.teamName = :newName
         where r.teamId  = :teamId
           and r.status in :statuses
    """)
    int renameTeam(@Param("teamId")    Integer teamId,
                   @Param("newName")   String  newName,
                   @Param("statuses")  List<ReceiptProcessStatus> statuses);

    @Modifying @Transactional
    @Query("""
        update Receipt r
           set r.teamId   		= null,
               r.teamName 		= null,
	           r.departmentId   = null,
	           r.departmentName = null
         where r.teamId  = :teamId
           and r.status in :statuses
    """)
    int nullifyTeam(@Param("teamId")   Integer teamId,
                    @Param("statuses") List<ReceiptProcessStatus> statuses);
    
    
    /* ---------- 사용자 부서/팀 동기화 ---------- */
    @Modifying @Transactional
    @Query("""
        update Receipt r
           set r.departmentId   = :deptId ,
               r.departmentName = :deptName ,
               r.teamId         = :teamId ,
               r.teamName       = :teamName
         where r.userId = :uid
           and r.status in :statuses
    """)
    int updateUserDeptTeam(@Param("uid")       Integer uid,
                           @Param("deptId")    Integer deptId,
                           @Param("deptName")  String  deptName,
                           @Param("teamId")    Integer teamId,
                           @Param("teamName")  String  teamName,
                           @Param("statuses")  List<ReceiptProcessStatus> statuses);

}
