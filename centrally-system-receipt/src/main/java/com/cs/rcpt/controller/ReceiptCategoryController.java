package com.cs.rcpt.controller;

import com.cs.rcpt.controller.request.ReceiptCategoryDto;
import com.cs.rcpt.entity.ReceiptCategory;
import com.cs.rcpt.service.ReceiptCategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class ReceiptCategoryController {

    private final ReceiptCategoryService receiptCategoryService;

    /** 활성 목록만 */
    @GetMapping
    public ResponseEntity<List<ReceiptCategory>> getCategories() {
        return ResponseEntity.ok(receiptCategoryService.getCategories());
    }

    /**
     * 비활성화된 카테고리 목록 조회
     */
    @GetMapping("/all")
    public ResponseEntity<List<ReceiptCategory>> getCategoriesWithDisabled() {
        return ResponseEntity.ok(receiptCategoryService.getCategoriesWithDisabled());
    }

    /** 등록 */
    @PostMapping
    public ResponseEntity<ReceiptCategory> createCategorys(@RequestBody @Valid ReceiptCategoryDto dto) {
        ReceiptCategory saved = receiptCategoryService.createCategorys(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /** 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<ReceiptCategory> updateCategorys(
		@PathVariable("id") Integer id,
		@RequestBody @Valid ReceiptCategoryDto dto
	) {
        return ResponseEntity.ok(receiptCategoryService.updateCategorys(id, dto));
    }

    /** 카테고리 비활성화 */
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Integer id) {
        receiptCategoryService.deleteCategory(id);
        return ResponseEntity.noContent().build(); // HttpStatus.NO_CONTENT 와 동일
    }
}
