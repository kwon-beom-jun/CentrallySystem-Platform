package com.cs.auth.system;

import com.cs.auth.outbox.OutboxEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        if (status != null && !status.isBlank()) {
            where.add(cb.equal(root.get("status"), OutboxEvent.St.valueOf(status)));
        }
        if (aggregateType != null && !aggregateType.isBlank()) {
            where.add(cb.equal(root.get("aggregateType"), aggregateType));
        }
        if (type != null && !type.isBlank()) {
            where.add(cb.equal(root.get("type"), type));
        }
        if (fromDate != null) {
            Instant from = fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            where.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }
        if (toDate != null) {
            Instant to = toDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant();
            where.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }
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
