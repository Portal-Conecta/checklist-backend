package com.portal.conecta.checklist.modules.checklist.issues.application.port.out.persistence;

import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;
import java.time.LocalDate;
import java.util.List;

/**
 * Port de saída (agregação) para métricas de não-conformidades (issues).
 *
 * <p>Todas as consultas realizam GROUP BY diretamente no banco — nenhuma entidade
 * é carregada em memória.</p>
 */
public interface ChecklistIssueStatsPort {

    /**
     * Contagem de issues por dia de criação ({@code CAST(due_at AS date)}).
     *
     * @param from início do intervalo (inclusive)
     * @param to   fim do intervalo (inclusive)
     */
    List<StatsEntryDTO> countByDay(LocalDate from, LocalDate to);

    /** Contagem de issues agrupadas por status. */
    List<StatsEntryDTO> countByStatus();

    /** Contagem de issues agrupadas por prioridade. */
    List<StatsEntryDTO> countByPriority();

    /** Divisão entre issues abertas ({@code resolved_at IS NULL}) e resolvidas. */
    List<StatsEntryDTO> resolutionSplit();

    /** Taxa de resolução: issues com {@code resolved_at IS NOT NULL} / total. */
    List<StatsEntryDTO> resolutionRate();

    /**
     * Tempo médio de resolução em segundos.
     * Considera apenas issues com {@code resolved_at IS NOT NULL}.
     */
    List<StatsEntryDTO> avgResolutionTime();

    /** Total de issues vencidas ({@code due_at < now()} e {@code resolved_at IS NULL}). */
    List<StatsEntryDTO> overdueCount();

    /**
     * Top itens que mais geram issues, limitado a {@code limit} entradas.
     *
     * @param limit número máximo de itens a retornar
     */
    List<StatsEntryDTO> topFailingItems(int limit);

    /** Contagem de issues agrupadas por tipo de checklist da execução vinculada. */
    List<StatsEntryDTO> countByChecklistType();

    /**
     * Média de issues por execução de checklist ({@code total issues / distinct executions}).
     */
    List<StatsEntryDTO> issuesPerExecution();
}
