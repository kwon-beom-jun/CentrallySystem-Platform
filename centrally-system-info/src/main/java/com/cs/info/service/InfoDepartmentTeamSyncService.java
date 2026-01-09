package com.cs.info.service;

import com.cs.core.kafka.event.hrm.DepartmentCreatedEvent;
import com.cs.core.kafka.event.hrm.DepartmentDeletedEvent;
import com.cs.core.kafka.event.hrm.DepartmentRenamedEvent;
import com.cs.core.kafka.event.hrm.TeamCreatedEvent;
import com.cs.core.kafka.event.hrm.TeamDeletedEvent;
import com.cs.core.kafka.event.hrm.TeamRenamedEvent;

/**
 * HRM 서비스의 부서/팀 변경 이벤트를 수신하여 INFO 서비스에 동기화하는 서비스 인터페이스
 */
public interface InfoDepartmentTeamSyncService {

    /**
     * 부서 생성 동기화
     * @param event 부서 생성 이벤트
     */
    void syncDepartmentCreated(DepartmentCreatedEvent event);

    /**
     * 부서명 변경 동기화
     * @param event 부서명 변경 이벤트
     */
    void syncDepartmentRenamed(DepartmentRenamedEvent event);

    /**
     * 부서 삭제 동기화 (Soft Delete)
     * @param event 부서 삭제 이벤트
     */
    void syncDepartmentDeleted(DepartmentDeletedEvent event);

    /**
     * 팀 생성 동기화
     * @param event 팀 생성 이벤트
     */
    void syncTeamCreated(TeamCreatedEvent event);

    /**
     * 팀명 변경 동기화
     * @param event 팀명 변경 이벤트
     */
    void syncTeamRenamed(TeamRenamedEvent event);

    /**
     * 팀 삭제 동기화 (Soft Delete)
     * @param event 팀 삭제 이벤트
     */
    void syncTeamDeleted(TeamDeletedEvent event);
}
