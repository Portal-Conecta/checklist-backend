package com.portal.conecta.checklist.modules.checklist.infrastructure.persistence;

import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistSubmissionWindowStatsPort;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.AvgFillTimeEntryDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Adapter de persistÃªncia para mÃ©tricas de janelas de submissÃ£o.
 *
 * <p>Todas as queries sÃ£o nativas e retornam projeÃ§Ãµes escalares â€” nenhuma entidade
 * Ã© carregada em memÃ³ria.</p>
 */
@Repository
public class ChecklistSubmissionWindowStatsRepository implements ChecklistSubmissionWindowStatsPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<StatsEntryDTO> countByType() {
        String sql = """
                SELECT checklist_type AS label, COUNT(*) AS value
                FROM checklist_submission_window
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
                FROM checklist_submission_window
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
    public List<AvgFillTimeEntryDTO> avgDurationByType() {
        String sql = """
                SELECT checklist_type                    AS label,
                       AVG(duration_minutes)            AS avg_seconds
                FROM checklist_submission_window
                GROUP BY checklist_type
                ORDER BY checklist_type
                """;
        List<Object[]> rows = em.createNativeQuery(sql).getResultList();
        return rows.stream()
                .map(r -> new AvgFillTimeEntryDTO((String) r[0], toDouble(r[1])))
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
