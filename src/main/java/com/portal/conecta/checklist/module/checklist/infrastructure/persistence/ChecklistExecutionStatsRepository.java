package com.portal.conecta.checklist.module.checklist.infrastructure.persistence;

import com.portal.conecta.checklist.module.checklist.application.port.out.persistence.ChecklistExecutionStatsPort;
import com.portal.conecta.checklist.shared.stats.StatsEntryDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Adapter de persistência para métricas de execuções de checklist.
 *
 * <p>Todas as queries são nativas e retornam projeções escalares — nenhuma entidade
 * é carregada em memória. O timezone assumido para corte diário é o servidor do banco
 * (sem conversão explícita); alinhar com o banco se necessário.</p>
 */
@Repository
public class ChecklistExecutionStatsRepository implements ChecklistExecutionStatsPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<StatsEntryDTO> countByDay(LocalDate from, LocalDate to) {
        String sql = """
                SELECT CAST(started_at AS date)::text AS label,
                       COUNT(*)                       AS value
                FROM checklist_execution
                WHERE started_at >= CAST(:from AS date)
                  AND started_at <  CAST(:to AS date) + INTERVAL '1 day'
                GROUP BY CAST(started_at AS date)
                ORDER BY CAST(started_at AS date)
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
                FROM checklist_execution
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
    public List<StatsEntryDTO> countByType() {
        String sql = """
                SELECT checklist_type AS label, COUNT(*) AS value
                FROM checklist_execution
                GROUP BY checklist_type
                ORDER BY value DESC
                """;
        List<Object[]> rows = em.createNativeQuery(sql).getResultList();
        return rows.stream()
                .map(r -> new StatsEntryDTO((String) r[0], toLong(r[1])))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StatsEntryDTO> countByShift() {
        String sql = """
                SELECT shift AS label, COUNT(*) AS value
                FROM checklist_execution
                GROUP BY shift
                ORDER BY value DESC
                """;
        List<Object[]> rows = em.createNativeQuery(sql).getResultList();
        return rows.stream()
                .map(r -> new StatsEntryDTO((String) r[0], toLong(r[1])))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StatsEntryDTO> countByPeriod() {
        String sql = """
                SELECT period AS label, COUNT(*) AS value
                FROM checklist_execution
                GROUP BY period
                ORDER BY value DESC
                """;
        List<Object[]> rows = em.createNativeQuery(sql).getResultList();
        return rows.stream()
                .map(r -> new StatsEntryDTO((String) r[0], toLong(r[1])))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StatsEntryDTO> completionRate() {
        String sql = """
                SELECT COUNT(*) FILTER (WHERE submitted_at IS NOT NULL) AS submitted,
                       COUNT(*)                                         AS total
                FROM checklist_execution
                """;
        Object[] row = (Object[]) em.createNativeQuery(sql).getSingleResult();
        long submitted = toLong(row[0]);
        long total = toLong(row[1]);
        return List.of(
                new StatsEntryDTO("submitted", submitted),
                new StatsEntryDTO("total", total),
                new StatsEntryDTO("ratePercent", percentage(submitted, total))
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StatsEntryDTO> avgFillTimeByDay(LocalDate from, LocalDate to) {
        String sql = """
                SELECT CAST(started_at AS date)::text                                AS label,
                       AVG(EXTRACT(EPOCH FROM (submitted_at - started_at)))          AS avg_seconds
                FROM checklist_execution
                WHERE submitted_at IS NOT NULL
                  AND started_at >= CAST(:from AS date)
                  AND started_at <  CAST(:to AS date) + INTERVAL '1 day'
                GROUP BY CAST(started_at AS date)
                ORDER BY CAST(started_at AS date)
                """;
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("from", from.toString())
                .setParameter("to", to.toString())
                .getResultList();
        return rows.stream()
                .map(r -> new StatsEntryDTO((String) r[0], toDouble(r[1])))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StatsEntryDTO> countByDayAndStatus(LocalDate from, LocalDate to) {
        String sql = """
                SELECT CAST(started_at AS date)::text || '|' || status AS label,
                       COUNT(*)                                         AS value
                FROM checklist_execution
                WHERE started_at >= CAST(:from AS date)
                  AND started_at <  CAST(:to AS date) + INTERVAL '1 day'
                GROUP BY CAST(started_at AS date), status
                ORDER BY CAST(started_at AS date), status
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
    public List<StatsEntryDTO> withIssuesRate() {
        String sql = """
                SELECT (SELECT COUNT(DISTINCT ce.id)
                        FROM checklist_execution ce
                        INNER JOIN checklist_issue ci ON ci.checklist_execution_id = ce.id)
                           AS with_issues,
                       COUNT(id)
                           AS total
                FROM checklist_execution
                WHERE status = 'SUBMITTED'
                """;
        Object[] row = (Object[]) em.createNativeQuery(sql).getSingleResult();
        long executionsWithIssues = toLong(row[0]);
        long totalExecutions = toLong(row[1]);
        return List.of(
                new StatsEntryDTO("executionsWithIssues", executionsWithIssues),
                new StatsEntryDTO("totalExecutions", totalExecutions),
                new StatsEntryDTO("ratePercent", percentage(executionsWithIssues, totalExecutions))
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StatsEntryDTO> heatmapShiftByDayOfWeek() {
        String sql = """
                SELECT shift                                   AS shift,
                       EXTRACT(DOW FROM started_at)::int       AS day_of_week,
                       COUNT(*)                                AS count
                FROM checklist_execution
                GROUP BY shift, EXTRACT(DOW FROM started_at)
                ORDER BY shift, day_of_week
                """;
        List<Object[]> rows = em.createNativeQuery(sql).getResultList();
        return rows.stream()
                .map(r -> new StatsEntryDTO(
                        r[0] + "|" + ((Number) r[1]).intValue(),
                        toLong(r[2])
                ))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StatsEntryDTO> complianceByShift() {
        String sql = """
                SELECT shift || '|' ||
                       CASE WHEN compliance_score >= 80 THEN 'ok'
                            WHEN compliance_score >= 50 THEN 'atencao'
                            ELSE 'critico' END AS label,
                       COUNT(*)                AS value
                FROM checklist_execution
                WHERE status = 'SUBMITTED' AND compliance_score IS NOT NULL
                GROUP BY shift,
                       CASE WHEN compliance_score >= 80 THEN 'ok'
                            WHEN compliance_score >= 50 THEN 'atencao'
                            ELSE 'critico' END
                ORDER BY shift
                """;
        List<Object[]> rows = em.createNativeQuery(sql).getResultList();
        return rows.stream()
                .map(r -> new StatsEntryDTO((String) r[0], toLong(r[1])))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StatsEntryDTO> complianceTrendByWeek(LocalDate from, LocalDate to) {
        String sql = """
                SELECT CAST(DATE_TRUNC('week', submitted_at) AS date)::text AS label,
                       AVG(compliance_score)                                AS value
                FROM checklist_execution
                WHERE status = 'SUBMITTED'
                  AND submitted_at IS NOT NULL
                  AND compliance_score IS NOT NULL
                  AND submitted_at >= CAST(:from AS date)
                  AND submitted_at <  CAST(:to AS date) + INTERVAL '1 day'
                GROUP BY DATE_TRUNC('week', submitted_at)
                ORDER BY DATE_TRUNC('week', submitted_at)
                """;
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("from", from.toString())
                .setParameter("to", to.toString())
                .getResultList();
        return rows.stream()
                .map(r -> new StatsEntryDTO((String) r[0], toDouble(r[1])))
                .toList();
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    private static long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private static double toDouble(Object value) {
        return value == null ? 0.0 : ((Number) value).doubleValue();
    }

    private static double percentage(long numerator, long denominator) {
        return denominator == 0 ? 0.0 : Math.round(numerator * 10000.0 / denominator) / 100.0;
    }
}
