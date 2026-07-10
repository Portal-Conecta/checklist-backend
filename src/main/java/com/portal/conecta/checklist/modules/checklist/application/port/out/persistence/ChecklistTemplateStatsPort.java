package com.portal.conecta.checklist.modules.checklist.application.port.out.persistence;

import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;
import java.util.List;

/**
 * Port de saÃ­da (agregaÃ§Ã£o) para mÃ©tricas de templates de checklist.
 *
 * <p>Todas as consultas realizam GROUP BY diretamente no banco â€” nenhuma entidade
 * Ã© carregada em memÃ³ria.</p>
 */
public interface ChecklistTemplateStatsPort {

    /** Contagem de templates agrupados por status ({@code DRAFT}, {@code ACTIVE}, {@code INACTIVE}). */
    List<StatsEntryDTO> countByStatus();

    /** Contagem de templates agrupados por flag {@code active} ({@code true}/{@code false}). */
    List<StatsEntryDTO> countByActive();

    /** Contagem de templates criados por dia ({@code CAST(created_at AS date)}). */
    List<StatsEntryDTO> countByDay();

    /**
     * NÃºmero de versÃµes por grupo de template ({@code template_group_id}).
     * O label Ã© o UUID do grupo como string.
     */
    List<StatsEntryDTO> countVersionsByGroup();
}
