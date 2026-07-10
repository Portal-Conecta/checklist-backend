package com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query;

import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistExecutionStatsPort;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.AvgFillTimeEntryDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.CompletionRateDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.HeatmapEntryDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.WithIssuesRateDTO;
import com.portal.conecta.checklist.shared.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Caso de uso para agregaÃ§Ã£o de mÃ©tricas de execuÃ§Ãµes de checklist.
 *
 * <p>Delega integralmente ao {@link ChecklistExecutionStatsPort} â€” toda a agregaÃ§Ã£o
 * acontece no banco. O UseCase Ã© responsÃ¡vel apenas por validaÃ§Ãµes de entrada,
 * regras de acesso e composiÃ§Ã£o de dados quando necessÃ¡rio.</p>
 */
@Service
@RequiredArgsConstructor
public class ChecklistExecutionStatsUseCase {

    private final ChecklistExecutionStatsPort statsPort;

    /**
     * Contagem de execuÃ§Ãµes por dia dentro do intervalo.
     *
     * @param from inÃ­cio do intervalo (inclusive); padrÃ£o: 30 dias atrÃ¡s
     * @param to   fim do intervalo (inclusive); padrÃ£o: hoje
     */
    public List<StatsEntryDTO> countByDay(LocalDate from, LocalDate to) {
        validateDateRange(from, to);
        LocalDate resolvedFrom = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate resolvedTo   = to   != null ? to   : LocalDate.now();
        return statsPort.countByDay(resolvedFrom, resolvedTo);
    }

    /** Contagem de execuÃ§Ãµes por status. */
    public List<StatsEntryDTO> countByStatus() {
        return statsPort.countByStatus();
    }

    /** Contagem de execuÃ§Ãµes por tipo de checklist. */
    public List<StatsEntryDTO> countByType() {
        return statsPort.countByType();
    }

    /** Contagem de execuÃ§Ãµes por turno. */
    public List<StatsEntryDTO> countByShift() {
        return statsPort.countByShift();
    }

    /** Contagem de execuÃ§Ãµes por perÃ­odo. */
    public List<StatsEntryDTO> countByPeriod() {
        return statsPort.countByPeriod();
    }

    /** Taxa de conclusÃ£o geral. */
    public CompletionRateDTO completionRate() {
        return statsPort.completionRate();
    }

    /**
     * Tempo mÃ©dio de preenchimento por dia.
     *
     * @param from inÃ­cio do intervalo; padrÃ£o: 30 dias atrÃ¡s
     * @param to   fim do intervalo; padrÃ£o: hoje
     */
    public List<AvgFillTimeEntryDTO> avgFillTimeByDay(LocalDate from, LocalDate to) {
        validateDateRange(from, to);
        LocalDate resolvedFrom = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate resolvedTo   = to   != null ? to   : LocalDate.now();
        return statsPort.avgFillTimeByDay(resolvedFrom, resolvedTo);
    }

    /**
     * SÃ©rie temporal de contagens por (dia, status).
     * O {@code label} de cada entrada tem o formato {@code YYYY-MM-DD|STATUS}.
     *
     * @param from inÃ­cio do intervalo; padrÃ£o: 30 dias atrÃ¡s
     * @param to   fim do intervalo; padrÃ£o: hoje
     */
    public List<StatsEntryDTO> countByDayAndStatus(LocalDate from, LocalDate to) {
        validateDateRange(from, to);
        LocalDate resolvedFrom = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate resolvedTo   = to   != null ? to   : LocalDate.now();
        return statsPort.countByDayAndStatus(resolvedFrom, resolvedTo);
    }

    /** Percentual de execuÃ§Ãµes que geraram ao menos 1 issue. */
    public WithIssuesRateDTO withIssuesRate() {
        return statsPort.withIssuesRate();
    }

    /** Heatmap de execuÃ§Ãµes por turno Ã— dia da semana. */
    public List<HeatmapEntryDTO> heatmap() {
        return statsPort.heatmapShiftByDayOfWeek();
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
