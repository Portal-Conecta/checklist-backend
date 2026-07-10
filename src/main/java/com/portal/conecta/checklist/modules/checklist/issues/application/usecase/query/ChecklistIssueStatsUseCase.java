package com.portal.conecta.checklist.modules.checklist.issues.application.usecase.query;

import com.portal.conecta.checklist.modules.checklist.issues.application.port.out.persistence.ChecklistIssueStatsPort;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.AvgResolutionTimeDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.IssuesPerExecutionDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.OverdueIssuesDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.ResolutionRateDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.ResolutionSplitDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.StatsEntryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Caso de uso para agregação de métricas de não-conformidades (issues).
 *
 * <p>Delega integralmente ao {@link ChecklistIssueStatsPort} — toda a agregação
 * acontece no banco.</p>
 */
@Service
@RequiredArgsConstructor
public class ChecklistIssueStatsUseCase {

    /** Limite máximo permitido para o parâmetro {@code limit} no top de itens. */
    private static final int MAX_LIMIT = 100;

    private final ChecklistIssueStatsPort statsPort;

    /**
     * Contagem de issues por dia dentro do intervalo.
     *
     * @param from início do intervalo; padrão: 30 dias atrás
     * @param to   fim do intervalo; padrão: hoje
     */
    public List<StatsEntryDTO> countByDay(LocalDate from, LocalDate to) {
        LocalDate resolvedFrom = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate resolvedTo   = to   != null ? to   : LocalDate.now();
        return statsPort.countByDay(resolvedFrom, resolvedTo);
    }

    /** Contagem de issues por status. */
    public List<StatsEntryDTO> countByStatus() {
        return statsPort.countByStatus();
    }

    /** Contagem de issues por prioridade. */
    public List<StatsEntryDTO> countByPriority() {
        return statsPort.countByPriority();
    }

    /** Divisão entre issues abertas e resolvidas. */
    public ResolutionSplitDTO resolutionSplit() {
        return statsPort.resolutionSplit();
    }

    /** Taxa de resolução de issues. */
    public ResolutionRateDTO resolutionRate() {
        return statsPort.resolutionRate();
    }

    /** Tempo médio de resolução em segundos. */
    public AvgResolutionTimeDTO avgResolutionTime() {
        return statsPort.avgResolutionTime();
    }

    /** Total de issues vencidas e não resolvidas. */
    public OverdueIssuesDTO overdueCount() {
        return statsPort.overdueCount();
    }

    /**
     * Top itens que mais geram issues.
     *
     * @param limit número de itens a retornar; limitado a {@value MAX_LIMIT}; padrão: 10
     */
    public List<StatsEntryDTO> topFailingItems(Integer limit) {
        int resolvedLimit = Math.min(limit != null ? limit : 10, MAX_LIMIT);
        return statsPort.topFailingItems(resolvedLimit);
    }

    /** Contagem de issues por tipo de checklist da execução vinculada. */
    public List<StatsEntryDTO> countByChecklistType() {
        return statsPort.countByChecklistType();
    }

    /** Média de issues por execução de checklist. */
    public IssuesPerExecutionDTO issuesPerExecution() {
        return statsPort.issuesPerExecution();
    }
}
