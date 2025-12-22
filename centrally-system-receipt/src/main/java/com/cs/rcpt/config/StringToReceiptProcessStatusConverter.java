package com.cs.rcpt.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.cs.rcpt.enums.ReceiptProcessStatus;

// 숫자 또는 이름 어느 쪽이든 ReceiptProcessStatus 로 변환해 주는 스프링 컨버
@Component
public class StringToReceiptProcessStatusConverter implements Converter<String, ReceiptProcessStatus> {

	@Override
	public ReceiptProcessStatus convert(String source) {
		if (source == null || source.isBlank())
			return null;

		// 숫자면 code 로, 아니면 enum 이름으로
		if (source.chars().allMatch(Character::isDigit)) {
			return ReceiptProcessStatus.of(Integer.parseInt(source));
		}
		return ReceiptProcessStatus.valueOf(source.toUpperCase());
	}
}
