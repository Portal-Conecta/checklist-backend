package com.portal.conecta.checklist.modules.checklist.infrastructure.persistence;

import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistExecutionStatsPort;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.AvgFillTimeEntryDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.CompletionRateDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.HeatmapEntryDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.WithIssuesRateDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Adapter de persistÃªncia para mÃ©tricas de execuÃ§Ãµes de checklist.
 *
 * <p>Todas as queries sÃ£o nativas e retornam projeÃ§Ãµes escalares â€” nenhuma entidade
 * Ã© carregada em memÃ³ria. O timezone assumido para corte diÃ¡rio Ã© o servidor do banco
 * (sem conversÃ£o explÃ­cita); alinhar com o banco se necessÃ¡rio.</p>
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
    public CompletionRateDTO completionRate() {
        String sql = """
                SELECT COUNT(*) FILTER (WHERE submitted_at IS NOT NULL) AS submitted,
                       COUNT(*)                                         AS total
                FROM checklist_execution
                """;
        Object[] row = (Object[]) em.createNativeQuery(sql).getSingleResult();
        return CompletionRateDTO.of(toLong(row[0]), toLong(row[1]));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AvgFillTimeEntryDTO> avgFillTimeByDay(LocalDate from, LocalDate to) {
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
                .map(r -> new AvgFillTimeEntryDTO((String) r[0], toDouble(r[1])))
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
    public WithIssuesRateDTO withIssuesRate() {
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
        return WithIssuesRateDTO.of(toLong(row[0]), toLong(row[1]));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<HeatmapEntryDTO> heatmapShiftByDayOfWeek() {
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
                .map(r -> new HeatmapEntryDTO(
                        (String) r[0],
                        ((Number) r[1]).intValue(),
                        toLong(r[2])
                ))
                .toList();
    }

    // â”€â”€â”€ helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private static long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private static double toDouble(Object value) {
        return value == null ? 0.0 : ((Number) value).doubleValue();
    }
}
