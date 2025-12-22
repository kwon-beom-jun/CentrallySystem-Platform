package com.cs.core.filter;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 모든 @Transactional 범위에서 Hibernate Filter("enabledFilter")를 활성화해
 * enabled = true(활성 레코드)만 조회하도록 만든다.
 *
 * ─ SoftDeleteEntity에 정의된 @FilterDef/@Filter와 연동 ─
 *
 * ▷ 삭제            : JpaRepository.delete*() → @SQLDelete ⇒ enabled = false 로 UPDATE
 * ▷ 일반 조회        : 자동 필터(enabled = true) 적용
 * ▷ 비활성 포함 조회 : 필요한 서비스 메서드 내부에서 session.disableFilter("enabledFilter") 호출
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class EnabledFilterAspect {

    private final EntityManager em;

    /**  
     * 모든 @Transactional 메서드 진입 시 enabledFilter 활성화  
     */
    @Around("@annotation(transactional)")
    public Object applyEnabledFilter(ProceedingJoinPoint pjp, Transactional transactional) throws Throwable {
        Session session = em.unwrap(Session.class);
        Filter filter = session.enableFilter("enabledFilter")
                               .setParameter("isEnabled", true);

        log.trace("enabledFilter ON (isEnabled=true) - {}", pjp.getSignature());
        try {
            return pjp.proceed();
        } finally {
            session.disableFilter("enabledFilter");
            log.trace("enabledFilter OFF - {}", pjp.getSignature());
        }
    }
}
