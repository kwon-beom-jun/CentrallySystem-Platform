package com.cs.core.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.cs.core.service.PagingService;
import com.cs.core.vo.page.PageResponseVo;

/**
 * 공통 페이징 서비스 구현체
 * 
 * 서비스에서 공통 페이징 로직 사용하기
 * 이제 각 엔티티 서비스에서 PagingService를 호출하여 페이징을 처리할 수 있습니다.
 */
@Service
public class PagingServiceImpl implements PagingService {

    @Override
    public <T> PageResponseVo<T> getPagedData(Pageable pageable, JpaRepository<T, Long> repository) {
        Page<T> pageData = repository.findAll(pageable);
        return new PageResponseVo<T>(pageData);
    }
}

