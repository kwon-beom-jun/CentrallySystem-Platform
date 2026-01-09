package com.cs.auth.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cs.auth.entity.Agreement;
import com.cs.auth.repository.AgreementRepository;
import com.cs.auth.service.AgreementService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AgreementServiceImpl implements AgreementService {

    private final AgreementRepository agreementRepository;
    private static final String DEFAULT_LANGUAGE_CODE = "ko";

    /**
     * 약관별 최신 버전만 반환 (title 단위 그룹핑)
     * @param languageCode 프론트에서 요청한 언어 코드 (ISO 639-1)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Agreement> getLatestAgreements(String languageCode) {
        String targetLanguage = resolveLanguageCode(languageCode);

        List<Agreement> agreements = agreementRepository.findByLanguageCode(targetLanguage);

        if (agreements.isEmpty() && !DEFAULT_LANGUAGE_CODE.equals(targetLanguage)) {
            agreements = agreementRepository.findByLanguageCode(DEFAULT_LANGUAGE_CODE);
        }

        // 전체 조회 후 title 로 그룹핑 → effectiveDate 최신 선택
        Map<String, Agreement> latestMap = agreements.stream()
                .collect(Collectors.toMap(
                        Agreement::getTitle,
                        a -> a,
                        (a1, a2) -> a1.getEffectiveDate().isAfter(a2.getEffectiveDate()) ? a1 : a2));

        return latestMap.values().stream()
                .sorted(Comparator.comparing(Agreement::getIsRequired).reversed())
                .toList();
    }

    /**
     * 비어있는 언어 코드를 기본 언어 코드로 대체한다.
     */
    private String resolveLanguageCode(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return DEFAULT_LANGUAGE_CODE;
        }
        return languageCode.toLowerCase();
    }
}

