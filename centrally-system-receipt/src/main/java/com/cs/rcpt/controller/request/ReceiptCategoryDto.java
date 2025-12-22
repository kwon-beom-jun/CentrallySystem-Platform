package com.cs.rcpt.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/** 카테고리 등록 / 수정 요청 DTO */
@Getter @Setter
public class ReceiptCategoryDto {

    /** 카테고리명: 공백 불가 */
    @NotBlank(message = "카테고리명은 필수입니다")
    private String categoryName;

    /** 한도 금액: null 불가, 1원 이상 */
    @NotNull(message = "한도 금액을 입력하세요")
    @Min(value = 1, message = "한도 금액은 1원 이상이어야 합니다")
    private Integer limitPrice;
}
