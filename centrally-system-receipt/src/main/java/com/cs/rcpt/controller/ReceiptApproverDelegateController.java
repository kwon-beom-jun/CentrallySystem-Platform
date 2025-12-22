package com.cs.rcpt.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cs.rcpt.entity.ReceiptApproverDelegate;
import com.cs.rcpt.service.ReceiptApproverDelegateService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/delegate-approvals", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ReceiptApproverDelegateController {

    private final ReceiptApproverDelegateService delegateService;

    /** 활성 대리자 조회 */
    @GetMapping("/{principalUserId}")
    public ResponseEntity<ReceiptApproverDelegate> getActiveDelegate(
            @PathVariable("principalUserId") Integer principalUserId) {
        return delegateService.getActiveDelegate(principalUserId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /** 나(=delegateUserId)를 대리자로 지정한 활성 원결재자 목록 */
    @GetMapping("/by-delegate/{delegateUserId}")
    public ResponseEntity<java.util.List<ReceiptApproverDelegate>> getActivePrincipalsByDelegate(
            @PathVariable("delegateUserId") Integer delegateUserId) {
        return ResponseEntity.ok(delegateService.getActivePrincipalsByDelegate(delegateUserId));
    }

    /** 대리자 매핑 저장 */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReceiptApproverDelegate> createDelegate(@RequestBody ReceiptApproverDelegate mapping) {
        mapping.setEnabled(true); // SoftDeleteEntity 활성화 기본값
        return ResponseEntity.ok(delegateService.createDelegate(mapping));
    }

    /** 대리자 매핑 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDelegate(@PathVariable("id") Long id) {
        delegateService.deleteDelegate(id);
        return ResponseEntity.noContent().build();
    }

    /** 특정 사용자와 관련된 모든 대리자 매핑(원/대리) 소프트 삭제 */
    @DeleteMapping("/by-user/{userId}")
    public ResponseEntity<Integer> deleteAllMappingsByUser(@PathVariable("userId") Integer userId) {
        int updated = delegateService.softDeleteAllMappingsByUser(userId);
        return ResponseEntity.ok(updated);
    }
}
