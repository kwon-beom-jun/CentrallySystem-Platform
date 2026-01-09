package com.cs.rcpt.controller;

import com.cs.rcpt.controller.request.DepartmentUpdateRequest;
import com.cs.rcpt.controller.request.TeamUpdateRequest;
import com.cs.rcpt.controller.request.UserDeptTeamUpdateRequest;
import com.cs.rcpt.service.ReceiptHrmSyncService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * HRM 서비스 전용 내부 API (게이트웨이 통과 X)
 * 인증은 내부 마이크로서비스 토큰만 수신한다는 가정.
 */
@RestController
@RequestMapping("/internal/hrm")
@RequiredArgsConstructor
public class ReceiptHrmSyncController {

    private final ReceiptHrmSyncService syncService;

    /* ---------- 부서 ---------- */

    @PatchMapping("/departments/{departmentId}")
    public ResponseEntity<Void> renameDepartment(
    		@PathVariable("departmentId") Integer deptId,
            @RequestBody @Valid DepartmentUpdateRequest req) {
        syncService.renameDepartment(deptId, req.getDepartmentName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/departments/{departmentId}")
    public ResponseEntity<Void> deleteDepartment(
    		@PathVariable("departmentId") Integer deptId) {
        syncService.deleteDepartment(deptId);
        return ResponseEntity.ok().build();
    }

    /* ---------- 팀 ---------- */

    @PatchMapping("/teams/{teamId}")
    public ResponseEntity<Void> renameTeam(
    		@PathVariable("teamId") Integer teamId,
            @RequestBody @Valid TeamUpdateRequest req) {
        syncService.renameTeam(teamId, req.getTeamName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/teams/{teamId}")
    public ResponseEntity<Void> deleteTeam(
    		@PathVariable("teamId") Integer teamId) {
        syncService.deleteTeam(teamId);
        return ResponseEntity.ok().build();
    }
    
    /* ---------- 사용자 부서/팀 동기화 ---------- */
    @PatchMapping("/users/{userId}/dept-team")
    public ResponseEntity<Void> updateUserDeptTeam(
    		@PathVariable("userId") Integer userId,
            @RequestBody @Valid UserDeptTeamUpdateRequest req) {

        syncService.updateUserDeptTeam(
                userId,
                req.getDepartmentId(), req.getDepartmentName(),
                req.getTeamId(),       req.getTeamName()
        );
        return ResponseEntity.ok().build();
    }
}
