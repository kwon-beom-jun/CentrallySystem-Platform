package com.cs.rcpt.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** 
 * ReceiptProcessStatus ↔ Integer(status_code) 변환기
 * - 엔티티 ↔ DB 컬럼 변환 ― 즉 JPA/Hibernate 영역 전용
 */
@Converter(autoApply = true)
public class ReceiptProcessStatusConverter
        implements AttributeConverter<ReceiptProcessStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ReceiptProcessStatus attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public ReceiptProcessStatus convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : ReceiptProcessStatus.fromCode(dbData);
    }
}
