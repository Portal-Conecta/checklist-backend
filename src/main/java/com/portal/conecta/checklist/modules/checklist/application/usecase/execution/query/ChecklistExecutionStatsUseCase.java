package com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query;

import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistExecutionStatsPort;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.AvgFillTimeEntryDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.CompletionRateDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.HeatmapEntryDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.StatsEntryDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.WithIssuesRateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Caso de uso para agregação de métricas de execuções de checklist.
 *
 * <p>Delega integralmente ao {@link ChecklistExecutionStatsPort} — toda a agregação
 * acontece no banco. O UseCase é responsável apenas por validações de entrada,
 * regras de acesso e composição de dados quando necessário.</p>
 */
@Service
@RequiredArgsConstructor
public class ChecklistExecutionStatsUseCase {

    private final ChecklistExecutionStatsPort statsPort;

    /**
     * Contagem de execuções por dia dentro do intervalo.
     *
     * @param from início do intervalo (inclusive); padrão: 30 dias atrás
     * @param to   fim do intervalo (inclusive); padrão: hoje
     */
    public List<StatsEntryDTO> countByDay(LocalDate from, LocalDate to) {
        LocalDate resolvedFrom = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate resolvedTo   = to   != null ? to   : LocalDate.now();
        return statsPort.countByDay(resolvedFrom, resolvedTo);
    }

    /** Contagem de execuções por status. */
    public List<StatsEntryDTO> countByStatus() {
        return statsPort.countByStatus();
    }

    /** Contagem de execuções por tipo de checklist. */
    public List<StatsEntryDTO> countByType() {
        return statsPort.countByType();
    }

    /** Contagem de execuções por turno. */
    public List<StatsEntryDTO> countByShift() {
        return statsPort.countByShift();
    }

    /** Contagem de execuções por período. */
    public List<StatsEntryDTO> countByPeriod() {
        return statsPort.countByPeriod();
    }

    /** Taxa de conclusão geral. */
    public CompletionRateDTO completionRate() {
        return statsPort.completionRate();
    }

    /**
     * Tempo médio de preenchimento por dia.
     *
     * @param from início do intervalo; padrão: 30 dias atrás
     * @param to   fim do intervalo; padrão: hoje
     */
    public List<AvgFillTimeEntryDTO> avgFillTimeByDay(LocalDate from, LocalDate to) {
        LocalDate resolvedFrom = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate resolvedTo   = to   != null ? to   : LocalDate.now();
        return statsPort.avgFillTimeByDay(resolvedFrom, resolvedTo);
    }

    /**
     * Série temporal de contagens por (dia, status).
     * O {@code label} de cada entrada tem o formato {@code YYYY-MM-DD|STATUS}.
     *
     * @param from início do intervalo; padrão: 30 dias atrás
     * @param to   fim do intervalo; padrão: hoje
     */
    public List<StatsEntryDTO> countByDayAndStatus(LocalDate from, LocalDate to) {
        LocalDate resolvedFrom = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate resolvedTo   = to   != null ? to   : LocalDate.now();
        return statsPort.countByDayAndStatus(resolvedFrom, resolvedTo);
    }

    /** Percentual de execuções que geraram ao menos 1 issue. */
    public WithIssuesRateDTO withIssuesRate() {
        return statsPort.withIssuesRate();
    }

    /** Heatmap de execuções por turno × dia da semana. */
    public List<HeatmapEntryDTO> heatmap() {
        return statsPort.heatmapShiftByDayOfWeek();
    }
}
