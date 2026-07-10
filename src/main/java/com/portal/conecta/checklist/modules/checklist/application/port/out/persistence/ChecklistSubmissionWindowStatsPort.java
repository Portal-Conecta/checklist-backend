package com.portal.conecta.checklist.modules.checklist.application.port.out.persistence;

import com.portal.conecta.checklist.modules.checklist.application.dto.stats.AvgFillTimeEntryDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;

import java.util.List;

/**
 * Port de saÃ­da (agregaÃ§Ã£o) para mÃ©tricas de janelas de submissÃ£o.
 *
 * <p>Todas as consultas realizam GROUP BY diretamente no banco â€” nenhuma entidade
 * Ã© carregada em memÃ³ria.</p>
 */
public interface ChecklistSubmissionWindowStatsPort {

    /** Contagem de janelas agrupadas por tipo de checklist ({@code checklistType}). */
    List<StatsEntryDTO> countByType();

    /** Contagem de janelas agrupadas por turno ({@code shift}). */
    List<StatsEntryDTO> countByShift();

    /**
     * DuraÃ§Ã£o mÃ©dia em minutos por tipo de checklist.
     * O label Ã© o tipo e o valor Ã© a mÃ©dia arredondada.
     * Reutiliza {@link AvgFillTimeEntryDTO} apenas para a projeÃ§Ã£o label + avgSeconds.
     */
    List<AvgFillTimeEntryDTO> avgDurationByType();
}
