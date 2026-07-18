package com.portal.conecta.checklist.module.checklist.application.port.out.persistence;

import com.portal.conecta.checklist.shared.stats.StatsEntryDTO;

import java.util.List;

/**
 * Port de saída (agregação) para métricas de janelas de submissão.
 *
 * <p>Todas as consultas realizam GROUP BY diretamente no banco — nenhuma entidade
 * é carregada em memória.</p>
 */
public interface ChecklistSubmissionWindowStatsPort {

    /** Contagem de janelas agrupadas por tipo de checklist ({@code checklistType}). */
    List<StatsEntryDTO> countByType();

    /** Contagem de janelas agrupadas por turno ({@code shift}). */
    List<StatsEntryDTO> countByShift();

    /**
     * Duração média em minutos por tipo de checklist.
     * O label é o tipo e o valor é a média arredondada.
     */
    List<StatsEntryDTO> avgDurationByType();
}
