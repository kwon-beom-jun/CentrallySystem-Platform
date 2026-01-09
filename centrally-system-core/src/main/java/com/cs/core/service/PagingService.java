package com.cs.core.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.cs.core.vo.page.PageResponseVo;

/**
 * 공통 페이징 서비스 인터페이스
 * 
 * 서비스에서 공통 페이징 로직 사용하기
 * 이제 각 엔티티 서비스에서 PagingService를 호출하여 페이징을 처리할 수 있습니다.
 */
public interface PagingService {

    /**
     * 페이징 데이터 조회
     * @param pageable 페이징 정보
     * @param repository JPA Repository
     * @param <T> 엔티티 타입
     * @return 페이징 응답 VO
     */
    <T> PageResponseVo<T> getPagedData(Pageable pageable, JpaRepository<T, Long> repository);
}
