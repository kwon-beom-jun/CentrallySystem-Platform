package com.cs.gateway.dao;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Repository;

import com.cs.gateway.entity.RoleChangeEvent;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class RoleChangeEventDao {

    private final R2dbcEntityTemplate tpl;

    /* INSERT */
    public Mono<Void> save(RoleChangeEvent e) {
        return tpl.insert(e).then();
    }

    /* 미확인 이벤트 존재 여부 */
    public Mono<Boolean> hasUnChecked(int userId) {
        return tpl.getDatabaseClient()
                  .sql("""
                       SELECT 1 FROM gateway_role_change_event
                        WHERE user_id = :uid
                          AND checked_at IS NULL
                        LIMIT 1
                       """)
                  .bind("uid", userId)
                  .map((row, meta) -> 1)
                  .one()
                  .hasElement();
    }

    /* checked_at 갱신 */
    public Mono<Void> markChecked(int userId) {
        return tpl.getDatabaseClient()
                  .sql("""
                       UPDATE gateway_role_change_event
                          SET checked_at = now()
                        WHERE user_id = :uid
                          AND checked_at IS NULL
                       """)
                  .bind("uid", userId)
                  .then();
    }
}
