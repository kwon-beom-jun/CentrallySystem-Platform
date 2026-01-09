package com.cs.rcpt.system;

import com.cs.rcpt.outbox.OutboxEvent;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@RestController
@RequestMapping
public class SystemMonitorController {

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/outbox")
    public Page<OutboxRow> getOutbox(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "aggregateType", required = false) String aggregateType,
            @RequestParam(value = "eventType", required = false) String type,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OutboxEvent> cq = cb.createQuery(OutboxEvent.class);
        Root<OutboxEvent> root = cq.from(OutboxEvent.class);

        List<Predicate> where = new ArrayList<>();
        if (status != null && !status.isBlank())
            where.add(cb.equal(root.get("status"), OutboxEvent.St.valueOf(status)));
        if (aggregateType != null && !aggregateType.isBlank())
            where.add(cb.equal(root.get("aggregateType"), aggregateType));
        if (type != null && !type.isBlank())
            where.add(cb.equal(root.get("type"), type));
        if (fromDate != null)
            where.add(cb.greaterThanOrEqualTo(root.get("createdAt"),
                    fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        if (toDate != null)
            where.add(cb.lessThanOrEqualTo(root.get("createdAt"),
                    toDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()));

        cq.where(where.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(root.get("createdAt")));

        Pageable pageable = PageRequest.of(page, size);
        TypedQuery<OutboxEvent> query = entityManager.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());

        List<OutboxEvent> result = query.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<OutboxEvent> countRoot = countQuery.from(OutboxEvent.class);
        List<Predicate> countWhere = new ArrayList<>();
        if (status != null && !status.isBlank())
            countWhere.add(cb.equal(countRoot.get("status"), OutboxEvent.St.valueOf(status)));
        if (aggregateType != null && !aggregateType.isBlank())
            countWhere.add(cb.equal(countRoot.get("aggregateType"), aggregateType));
        if (type != null && !type.isBlank())
            countWhere.add(cb.equal(countRoot.get("type"), type));
        if (fromDate != null)
            countWhere.add(cb.greaterThanOrEqualTo(countRoot.get("createdAt"),
                    fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        if (toDate != null)
            countWhere.add(cb.lessThanOrEqualTo(countRoot.get("createdAt"),
                    toDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()));
        countQuery.select(cb.count(countRoot)).where(countWhere.toArray(new Predicate[0]));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        List<OutboxRow> rows = result.stream().map(OutboxRow::from).toList();
        return new PageImpl<>(rows, pageable, total);
    }

    public record OutboxRow(
            UUID id,
            String aggregateType,
            String aggregateId,
            String eventType,
            String payload,
            String status,
            Instant createdAt,
            Instant lastTriedAt,
            String errorMessage) {
        public static OutboxRow from(OutboxEvent e) {
            return new OutboxRow(
                    e.getId(),
                    e.getAggregateType(),
                    e.getAggregateId(),
                    e.getType(),
                    e.getPayload(),
                    e.getStatus().name(),
                    e.getCreatedAt(),
                    e.getLastAttemptAt(),
                    e.getErrorMessage());
        }
    }

}
