package com.portal.conecta.checklist.modules.checklist.application.port.out.persistence;

import com.portal.conecta.checklist.modules.checklist.application.dto.stats.AvgFillTimeEntryDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.CompletionRateDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.HeatmapEntryDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.WithIssuesRateDTO;
import java.time.LocalDate;
import java.util.List;

/**
 * Port de saÃ­da (agregaÃ§Ã£o) para mÃ©tricas de execuÃ§Ãµes de checklist.
 *
 * <p>Todas as consultas realizam GROUP BY diretamente no banco â€” nenhuma entidade
 * Ã© carregada em memÃ³ria.</p>
 */
public interface ChecklistExecutionStatsPort {

    /**
     * Contagem de execuÃ§Ãµes por dia dentro do intervalo {@code [from, to]}.
     * A agregaÃ§Ã£o usa {@code CAST(started_at AS date)}.
     *
     * @param from inÃ­cio do intervalo (inclusive)
     * @param to   fim do intervalo (inclusive)
     */
    List<StatsEntryDTO> countByDay(LocalDate from, LocalDate to);

    /** Contagem de execuÃ§Ãµes agrupadas por status ({@code DRAFT}, {@code SUBMITTED}, {@code CANCELED}). */
    List<StatsEntryDTO> countByStatus();

    /** Contagem de execuÃ§Ãµes agrupadas por tipo de checklist ({@code checklistType}). */
    List<StatsEntryDTO> countByType();

    /** Contagem de execuÃ§Ãµes agrupadas por turno ({@code shift}). */
    List<StatsEntryDTO> countByShift();

    /** Contagem de execuÃ§Ãµes agrupadas por perÃ­odo ({@code period}). */
    List<StatsEntryDTO> countByPeriod();

    /**
     * Taxa de conclusÃ£o: execuÃ§Ãµes com {@code submitted_at IS NOT NULL} sobre o total.
     */
    CompletionRateDTO completionRate();

    /**
     * Tempo mÃ©dio de preenchimento em segundos, agrupado por dia.
     * Considera apenas execuÃ§Ãµes com {@code submitted_at IS NOT NULL}.
     *
     * @param from inÃ­cio do intervalo (inclusive)
     * @param to   fim do intervalo (inclusive)
     */
    List<AvgFillTimeEntryDTO> avgFillTimeByDay(LocalDate from, LocalDate to);

    /**
     * SÃ©rie temporal de contagens por (dia, status) para grÃ¡fico de linhas empilhadas.
     *
     * @param from inÃ­cio do intervalo (inclusive)
     * @param to   fim do intervalo (inclusive)
     */
    List<StatsEntryDTO> countByDayAndStatus(LocalDate from, LocalDate to);

    /**
     * Percentual de execuÃ§Ãµes que geraram ao menos uma issue.
     */
    WithIssuesRateDTO withIssuesRate();

    /**
     * Heatmap: contagem de execuÃ§Ãµes por turno e dia da semana.
     */
    List<HeatmapEntryDTO> heatmapShiftByDayOfWeek();
}
