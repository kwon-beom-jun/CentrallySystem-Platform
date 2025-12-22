package com.cs.rcpt.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cs.rcpt.controller.request.DefaultApproverDto;
import com.cs.rcpt.controller.request.StepOrderDto;
import com.cs.rcpt.service.ReceiptDefaultApproverService;

@RestController
@RequestMapping("/default-approvers")
@RequiredArgsConstructor
public class ReceiptDefaultApproverController {

    private final ReceiptDefaultApproverService svc;

    /** 전체 조회 (프런트 모달 진입 시 호출) */
    @GetMapping
    public Page<DefaultApproverDto> getDefaultApprovers(Pageable pageable) {
        return svc.getDefaultApprovers(pageable);
    }
    
    /** 특정 사용자가 고정 합의자인지 확인 */
    @GetMapping("/check/{userId}")
    public ResponseEntity<Boolean> checkIfDefaultApprover(@PathVariable("userId") Long userId) {
        boolean isDefault = svc.isDefaultApprover(userId);
        return ResponseEntity.ok(isDefault);
    }


    /** 신규 등록 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DefaultApproverDto createDefaultApprovers(@RequestBody DefaultApproverDto dto) {
        return svc.createDefaultApprovers(dto);
    }

    /** 삭제 */
    @DeleteMapping("{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDefaultApprovers(@PathVariable("userId") Long userId) {
        svc.deleteDefaultApprovers(userId);
    }
    
    /** 순서 변경 */
    @PatchMapping("/order")                         // ⬅️ 새 엔드포인트
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderDefaultApprovers(@RequestBody List<StepOrderDto> list) {
        svc.reorder(list);                          // 서비스에 위임
    }
}

