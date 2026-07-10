package com.portal.conecta.checklist.modules.checklist.issues.infrastructure.persistence;

import com.portal.conecta.checklist.modules.checklist.issues.application.port.out.persistence.ChecklistIssueStatsPort;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.AvgResolutionTimeDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.IssuesPerExecutionDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.OverdueIssuesDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.ResolutionRateDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.ResolutionSplitDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Adapter de persistência para métricas de não-conformidades (issues).
 *
 * <p>Todas as queries são nativas e retornam projeções escalares — nenhuma entidade
 * é carregada em memória.</p>
 *
 * <p>Timestamps com timezone: {@code due_at} e {@code resolved_at} são {@code TIMESTAMPTZ}
 * no banco. A comparação {@code due_at < now()} usa o timezone do servidor Postgres.</p>
 */
@Repository
public class ChecklistIssueStatsRepository implements ChecklistIssueStatsPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<StatsEntryDTO> countByDay(LocalDate from, LocalDate to) {
        String sql = """
                SELECT CAST(due_at AS date)::text AS label,
                       COUNT(*)                  AS value
                FROM checklist_issue
                WHERE due_at >= CAST(:from AS date)
                  AND due_at <  CAST(:to AS date) + INTERVAL '1 day'
                GROUP BY CAST(due_at AS date)
                ORDER BY CAST(due_at AS date)
                """;
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("from", from.toString())
                .setParameter("to", to.toString())
                .getResultList();
        return rows.stream()
                .map(r -> new StatsEntryDTO((String) r[0], toLong(r[1])))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StatsEntryDTO> countByStatus() {
        String sql = """
                SELECT status AS label, COUNT(*) AS value
                FROM checklist_issue
                GROUP BY status
                ORDER BY value DESC
                """;
        List<Object[]> rows = em.createNativeQuery(sql).getResultList();
        return rows.stream()
                .map(r -> new StatsEntryDTO((String) r[0], toLong(r[1])))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StatsEntryDTO> countByPriority() {
        String sql = """
                SELECT priority AS label, COUNT(*) AS value
                FROM checklist_issue
                GROUP BY priority
                ORDER BY value DESC
                """;
        List<Object[]> rows = em.createNativeQuery(sql).getResultList();
        return rows.stream()
                .map(r -> new StatsEntryDTO((String) r[0], toLong(r[1])))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ResolutionSplitDTO resolutionSplit() {
        String sql = """
                SELECT COUNT(*) FILTER (WHERE resolved_at IS NULL)     AS open,
                       COUNT(*) FILTER (WHERE resolved_at IS NOT NULL) AS resolved
                FROM checklist_issue
                """;
        Object[] row = (Object[]) em.createNativeQuery(sql).getSingleResult();
        return ResolutionSplitDTO.of(toLong(row[0]), toLong(row[1]));
    }

    @Override
    @SuppressWarnings("unchecked")
    public ResolutionRateDTO resolutionRate() {
        String sql = """
                SELECT COUNT(*) FILTER (WHERE resolved_at IS NOT NULL) AS resolved,
                       COUNT(*)                                        AS total
                FROM checklist_issue
                """;
        Object[] row = (Object[]) em.createNativeQuery(sql).getSingleResult();
        return ResolutionRateDTO.of(toLong(row[0]), toLong(row[1]));
    }

    @Override
    @SuppressWarnings("unchecked")
    public AvgResolutionTimeDTO avgResolutionTime() {
        String sql = """
                SELECT AVG(EXTRACT(EPOCH FROM (resolved_at - due_at))) AS avg_seconds
                FROM checklist_issue
                WHERE resolved_at IS NOT NULL
                """;
        Object result = em.createNativeQuery(sql).getSingleResult();
        return new AvgResolutionTimeDTO(toDouble(result));
    }

    @Override
    @SuppressWarnings("unchecked")
    public OverdueIssuesDTO overdueCount() {
        String sql = """
                SELECT COUNT(*) AS overdue
                FROM checklist_issue
                WHERE due_at < NOW()
                  AND resolved_at IS NULL
                """;
        Object result = em.createNativeQuery(sql).getSingleResult();
        return new OverdueIssuesDTO(toLong(result));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StatsEntryDTO> topFailingItems(int limit) {
        String sql = """
                SELECT item_key AS label, COUNT(*) AS value
                FROM checklist_issue
                GROUP BY item_key
                ORDER BY value DESC
                LIMIT :limit
                """;
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("limit", limit)
                .getResultList();
        return rows.stream()
                .map(r -> new StatsEntryDTO((String) r[0], toLong(r[1])))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StatsEntryDTO> countByChecklistType() {
        String sql = """
                SELECT ce.checklist_type AS label, COUNT(ci.id) AS value
                FROM checklist_issue ci
                INNER JOIN checklist_execution ce ON ce.id = ci.checklist_execution_id
                GROUP BY ce.checklist_type
                ORDER BY value DESC
                """;
        List<Object[]> rows = em.createNativeQuery(sql).getResultList();
        return rows.stream()
                .map(r -> new StatsEntryDTO((String) r[0], toLong(r[1])))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public IssuesPerExecutionDTO issuesPerExecution() {
        String sql = """
                SELECT COUNT(ci.id)                    AS total_issues,
                       COUNT(DISTINCT ci.checklist_execution_id) AS total_executions
                FROM checklist_issue ci
                """;
        Object[] row = (Object[]) em.createNativeQuery(sql).getSingleResult();
        return IssuesPerExecutionDTO.of(toLong(row[0]), toLong(row[1]));
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private static long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private static double toDouble(Object value) {
        return value == null ? 0.0 : ((Number) value).doubleValue();
    }
}
