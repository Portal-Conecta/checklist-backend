package com.portal.conecta.checklist.modules.checklist.issues.application.usecase.query;

import com.portal.conecta.checklist.modules.checklist.issues.application.port.out.persistence.ChecklistIssueStatsPort;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.AvgResolutionTimeDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.IssuesPerExecutionDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.OverdueIssuesDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.ResolutionRateDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.ResolutionSplitDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;
import com.portal.conecta.checklist.shared.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Caso de uso para agregaÃ§Ã£o de mÃ©tricas de nÃ£o-conformidades (issues).
 *
 * <p>Delega integralmente ao {@link ChecklistIssueStatsPort} â€” toda a agregaÃ§Ã£o
 * acontece no banco.</p>
 */
@Service
@RequiredArgsConstructor
public class ChecklistIssueStatsUseCase {

    /** Limite mÃ¡ximo permitido para o parÃ¢metro {@code limit} no top de itens. */
    private static final int MAX_LIMIT = 100;

    private final ChecklistIssueStatsPort statsPort;

    /**
     * Contagem de issues por dia dentro do intervalo.
     *
     * @param from inÃ­cio do intervalo; padrÃ£o: 30 dias atrÃ¡s
     * @param to   fim do intervalo; padrÃ£o: hoje
     */
    public List<StatsEntryDTO> countByDay(LocalDate from, LocalDate to) {
        validateDateRange(from, to);
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

    /** DivisÃ£o entre issues abertas e resolvidas. */
    public ResolutionSplitDTO resolutionSplit() {
        return statsPort.resolutionSplit();
    }

    /** Taxa de resoluÃ§Ã£o de issues. */
    public ResolutionRateDTO resolutionRate() {
        return statsPort.resolutionRate();
    }

    /** Tempo mÃ©dio de resoluÃ§Ã£o em segundos. */
    public AvgResolutionTimeDTO avgResolutionTime() {
        return statsPort.avgResolutionTime();
    }

    /** Total de issues vencidas e nÃ£o resolvidas. */
    public OverdueIssuesDTO overdueCount() {
        return statsPort.overdueCount();
    }

    /**
     * Top itens que mais geram issues.
     *
     * @param limit nÃºmero de itens a retornar; limitado a {@value MAX_LIMIT}; padrÃ£o: 10
     */
    public List<StatsEntryDTO> topFailingItems(Integer limit) {
        if (limit != null && limit < 1) {
            throw new InvalidRequestException(
                    "'limit' deve ser no mÃ­nimo 1. Valor informado: " + limit
            );
        }
        int resolvedLimit = Math.min(limit != null ? limit : 10, MAX_LIMIT);
        return statsPort.topFailingItems(resolvedLimit);
    }

    /** Contagem de issues por tipo de checklist da execuÃ§Ã£o vinculada. */
    public List<StatsEntryDTO> countByChecklistType() {
        return statsPort.countByChecklistType();
    }

    /** MÃ©dia de issues por execuÃ§Ã£o de checklist. */
    public IssuesPerExecutionDTO issuesPerExecution() {
        return statsPort.issuesPerExecution();
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // ValidaÃ§Ã£o auxiliar (defesa em profundidade)
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Valida o intervalo de datas â€” segunda linha de defesa apÃ³s o controller.
     *
     * @param from inÃ­cio do intervalo (pode ser {@code null})
     * @param to   fim do intervalo (pode ser {@code null})
     * @throws InvalidRequestException se {@code from > to}
     */
    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidRequestException(
                    "'from' (" + from + ") deve ser anterior ou igual a 'to' (" + to + ")"
            );
        }
    }
}
