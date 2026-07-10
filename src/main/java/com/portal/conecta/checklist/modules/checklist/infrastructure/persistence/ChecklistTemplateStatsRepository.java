package com.portal.conecta.checklist.modules.checklist.infrastructure.persistence;

import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistTemplateStatsPort;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Adapter de persistÃªncia para mÃ©tricas de templates de checklist.
 *
 * <p>Todas as queries sÃ£o nativas e retornam projeÃ§Ãµes escalares â€” nenhuma entidade
 * Ã© carregada em memÃ³ria.</p>
 */
@Repository
public class ChecklistTemplateStatsRepository implements ChecklistTemplateStatsPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    @SuppressWarnings("unchecked")
    public List<StatsEntryDTO> countByStatus() {
        String sql = """
                SELECT status AS label, COUNT(*) AS value
                FROM checklist_template
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
    public List<StatsEntryDTO> countByActive() {
        String sql = """
                SELECT active::text AS label, COUNT(*) AS value
                FROM checklist_template
                GROUP BY active
                ORDER BY active DESC
                """;
        List<Object[]> rows = em.createNativeQuery(sql).getResultList();
        return rows.stream()
                .map(r -> new StatsEntryDTO((String) r[0], toLong(r[1])))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StatsEntryDTO> countByDay() {
        String sql = """
                SELECT CAST(created_at AS date)::text AS label,
                       COUNT(*)                       AS value
                FROM checklist_template
                GROUP BY CAST(created_at AS date)
                ORDER BY CAST(created_at AS date)
                """;
        List<Object[]> rows = em.createNativeQuery(sql).getResultList();
        return rows.stream()
                .map(r -> new StatsEntryDTO((String) r[0], toLong(r[1])))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<StatsEntryDTO> countVersionsByGroup() {
        String sql = """
                SELECT template_group_id::text AS label,
                       COUNT(*)               AS value
                FROM checklist_template
                GROUP BY template_group_id
                ORDER BY value DESC
                """;
        List<Object[]> rows = em.createNativeQuery(sql).getResultList();
        return rows.stream()
                .map(r -> new StatsEntryDTO((String) r[0], toLong(r[1])))
                .toList();
    }

    // â”€â”€â”€ helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private static long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }
}
