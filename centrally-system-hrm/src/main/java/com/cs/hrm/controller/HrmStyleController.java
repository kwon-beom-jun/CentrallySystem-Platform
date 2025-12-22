package com.cs.hrm.controller;

import com.cs.hrm.entity.HrmStyle;
import com.cs.hrm.service.HrmStyleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * HRM 스타일 관리 컨트롤러
 */
@RestController
@RequestMapping("/styles")
@RequiredArgsConstructor
public class HrmStyleController {

    private final HrmStyleService hrmStyleService;

    /**
     * 모든 스타일 조회
     * GET /hrm/styles
     */
    @GetMapping
    public ResponseEntity<List<HrmStyle>> getAllStyles() {
        return ResponseEntity.ok(hrmStyleService.getAllStyles());
    }

    /**
     * 카테고리별 스타일 조회
     * GET /hrm/styles/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<HrmStyle>> getStylesByCategory(
            @PathVariable("category") String category) {
        return ResponseEntity.ok(hrmStyleService.getStylesByCategory(category));
    }

    /**
     * 사용자 스타일 설정 조회
     * GET /hrm/styles/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, String>> getUserStyles(
            @PathVariable("userId") Integer userId) {
        return ResponseEntity.ok(hrmStyleService.getUserStyleCodes(userId));
    }

    /**
     * 사용자 스타일 업데이트
     * PATCH /hrm/styles/user/{userId}
     */
    @PatchMapping("/user/{userId}")
    public ResponseEntity<Void> updateUserStyle(
            @PathVariable("userId") Integer userId,
            @RequestBody Map<String, String> request) {
        String category = request.get("category");
        String styleCode = request.get("styleCode");
        hrmStyleService.updateUserStyle(userId, category, styleCode);
        return ResponseEntity.ok().build();
    }
}

