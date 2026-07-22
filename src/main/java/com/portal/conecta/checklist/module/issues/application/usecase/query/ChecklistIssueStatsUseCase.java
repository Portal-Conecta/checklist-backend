package com.portal.conecta.checklist.module.issues.application.usecase.query;

import com.portal.conecta.checklist.module.issues.application.port.out.persistence.ChecklistIssueStatsPort;
import com.portal.conecta.checklist.shared.stats.StatsEntryDTO;
import com.portal.conecta.checklist.shared.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

    /** Divisão entre issues abertas e resolvidas. */
    public List<StatsEntryDTO> resolutionSplit() {
        return statsPort.resolutionSplit();
    }

    /** Taxa de resolução de issues. */
    public List<StatsEntryDTO> resolutionRate() {
        return statsPort.resolutionRate();
    }

    /** Tempo médio de resolução em segundos. */
    public List<StatsEntryDTO> avgResolutionTime() {
        return statsPort.avgResolutionTime();
    }

    /** Total de issues vencidas e não resolvidas. */
    public List<StatsEntryDTO> overdueCount() {
        return statsPort.overdueCount();
    }

    /**
     * Top itens que mais geram issues.
     *
     * @param limit número de itens a retornar; limitado a {@value MAX_LIMIT}; padrão: 10
     */
    public List<StatsEntryDTO> topFailingItems(Integer limit) {
        if (limit != null && limit < 1) {
            throw new InvalidRequestException(
                    "'limit' deve ser no mínimo 1. Valor informado: " + limit
            );
        }
        int resolvedLimit = Math.min(limit != null ? limit : 10, MAX_LIMIT);
        return statsPort.topFailingItems(resolvedLimit);
    }

    /** Contagem de issues por tipo de checklist da execução vinculada. */
    public List<StatsEntryDTO> countByChecklistType() {
        return statsPort.countByChecklistType();
    }

    /** Média de issues por execução de checklist. */
    public List<StatsEntryDTO> issuesPerExecution() {
        return statsPort.issuesPerExecution();
    }


    /**
     * Valida o intervalo de datas — segunda linha de defesa após o controller.
     *
     * @param from início do intervalo (pode ser {@code null})
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
