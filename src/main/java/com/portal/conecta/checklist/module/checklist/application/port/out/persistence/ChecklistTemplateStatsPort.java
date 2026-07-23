package com.portal.conecta.checklist.module.checklist.application.port.out.persistence;

import com.portal.conecta.checklist.shared.stats.StatsEntryDTO;
import java.util.List;

/**
 * Port de saída (agregação) para métricas de templates de checklist.
 *
 * <p>Todas as consultas realizam GROUP BY diretamente no banco — nenhuma entidade
 * é carregada em memória.</p>
 */
public interface ChecklistTemplateStatsPort {

    /** Contagem de templates agrupados por status ({@code DRAFT}, {@code ACTIVE}, {@code INACTIVE}). */
    List<StatsEntryDTO> countByStatus();

    /** Contagem de templates agrupados por flag {@code active} ({@code true}/{@code false}). */
    List<StatsEntryDTO> countByActive();

    /** Contagem de templates criados por dia ({@code CAST(created_at AS date)}). */
    List<StatsEntryDTO> countByDay();

    /**
     * Número de versões por grupo de template ({@code template_group_id}).
     * O label é o UUID do grupo como string.
     */
    List<StatsEntryDTO> countVersionsByGroup();
}
