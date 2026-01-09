package com.cs.auth.service;

import com.cs.auth.entity.Agreement;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 약관 관리 서비스 인터페이스
 */
public interface AgreementService {

    /**
     * 약관별 최신 버전만 반환 (title 단위 그룹핑)
     * @param languageCode 프론트에서 요청한 언어 코드 (ISO 639-1)
     * @return 최신 버전의 약관 목록
     */
    @Transactional(readOnly = true)
    List<Agreement> getLatestAgreements(String languageCode);
}
