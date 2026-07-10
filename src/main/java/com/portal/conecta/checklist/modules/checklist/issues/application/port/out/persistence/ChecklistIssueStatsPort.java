package com.portal.conecta.checklist.modules.checklist.issues.application.port.out.persistence;

import com.portal.conecta.checklist.modules.checklist.application.dto.stats.AvgResolutionTimeDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.IssuesPerExecutionDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.OverdueIssuesDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.ResolutionRateDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.ResolutionSplitDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;
import java.time.LocalDate;
import java.util.List;

/**
 * Port de saÃ­da (agregaÃ§Ã£o) para mÃ©tricas de nÃ£o-conformidades (issues).
 *
 * <p>Todas as consultas realizam GROUP BY diretamente no banco â€” nenhuma entidade
 * Ã© carregada em memÃ³ria.</p>
 */
public interface ChecklistIssueStatsPort {

    /**
     * Contagem de issues por dia de criaÃ§Ã£o ({@code CAST(due_at AS date)}).
     *
     * @param from inÃ­cio do intervalo (inclusive)
     * @param to   fim do intervalo (inclusive)
     */
    List<StatsEntryDTO> countByDay(LocalDate from, LocalDate to);

    /** Contagem de issues agrupadas por status. */
    List<StatsEntryDTO> countByStatus();

    /** Contagem de issues agrupadas por prioridade. */
    List<StatsEntryDTO> countByPriority();

    /** DivisÃ£o entre issues abertas ({@code resolved_at IS NULL}) e resolvidas. */
    ResolutionSplitDTO resolutionSplit();

    /** Taxa de resoluÃ§Ã£o: issues com {@code resolved_at IS NOT NULL} / total. */
    ResolutionRateDTO resolutionRate();

    /**
     * Tempo mÃ©dio de resoluÃ§Ã£o em segundos.
     * Considera apenas issues com {@code resolved_at IS NOT NULL}.
     */
    AvgResolutionTimeDTO avgResolutionTime();

    /** Total de issues vencidas ({@code due_at < now()} e {@code resolved_at IS NULL}). */
    OverdueIssuesDTO overdueCount();

    /**
     * Top itens que mais geram issues, limitado a {@code limit} entradas.
     *
     * @param limit nÃºmero mÃ¡ximo de itens a retornar
     */
    List<StatsEntryDTO> topFailingItems(int limit);

    /** Contagem de issues agrupadas por tipo de checklist da execuÃ§Ã£o vinculada. */
    List<StatsEntryDTO> countByChecklistType();

    /**
     * MÃ©dia de issues por execuÃ§Ã£o de checklist ({@code total issues / distinct executions}).
     */
    IssuesPerExecutionDTO issuesPerExecution();
}
