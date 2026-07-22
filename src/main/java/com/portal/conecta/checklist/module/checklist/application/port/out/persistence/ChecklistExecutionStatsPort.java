package com.portal.conecta.checklist.module.checklist.application.port.out.persistence;

import com.portal.conecta.checklist.shared.stats.StatsEntryDTO;
import java.time.LocalDate;
import java.util.List;

/**
 * Port de saída (agregação) para métricas de execuções de checklist.
 *
 * <p>Todas as consultas realizam GROUP BY diretamente no banco — nenhuma entidade
 * é carregada em memória.</p>
 */
public interface ChecklistExecutionStatsPort {

    /**
     * Contagem de execuções por dia dentro do intervalo {@code [from, to]}.
     * A agregação usa {@code CAST(started_at AS date)}.
     *
     * @param from início do intervalo (inclusive)
     * @param to   fim do intervalo (inclusive)
     */
    List<StatsEntryDTO> countByDay(LocalDate from, LocalDate to);

    /** Contagem de execuções agrupadas por status ({@code DRAFT}, {@code SUBMITTED}, {@code CANCELED}). */
    List<StatsEntryDTO> countByStatus();

    /** Contagem de execuções agrupadas por tipo de checklist ({@code checklistType}). */
    List<StatsEntryDTO> countByType();

    /** Contagem de execuções agrupadas por turno ({@code shift}). */
    List<StatsEntryDTO> countByShift();

    /** Contagem de execuções agrupadas por período ({@code period}). */
    List<StatsEntryDTO> countByPeriod();

    /**
     * Taxa de conclusão: execuções com {@code submitted_at IS NOT NULL} sobre o total.
     */
    List<StatsEntryDTO> completionRate();

    /**
     * Tempo médio de preenchimento em segundos, agrupado por dia.
     * Considera apenas execuções com {@code submitted_at IS NOT NULL}.
     *
     * @param from início do intervalo (inclusive)
     * @param to   fim do intervalo (inclusive)
     */
    List<StatsEntryDTO> avgFillTimeByDay(LocalDate from, LocalDate to);

    /**
     * Série temporal de contagens por (dia, status) para gráfico de linhas empilhadas.
     *
     * @param from início do intervalo (inclusive)
     * @param to   fim do intervalo (inclusive)
     */
    List<StatsEntryDTO> countByDayAndStatus(LocalDate from, LocalDate to);

    /**
     * Percentual de execuções que geraram ao menos uma issue.
     */
    List<StatsEntryDTO> withIssuesRate();

    /**
     * Heatmap: contagem de execuções por turno e dia da semana.
     */
    List<StatsEntryDTO> heatmapShiftByDayOfWeek();

    /**
     * Contagem de execuções submetidas por turno, agrupadas em 3 faixas de
     * {@code compliance_score} (ok &gt;= 80, atencao 50-79.99, critico &lt; 50).
     * O {@code label} tem o formato {@code SHIFT|BUCKET}. Execuções sem
     * {@code compliance_score} calculado (não submetidas) são ignoradas.
     */
    List<StatsEntryDTO> complianceByShift();

    /**
     * Média de {@code compliance_score} por semana, para execuções submetidas
     * no intervalo {@code [from, to]}. O {@code label} é a data (ISO,
     * YYYY-MM-DD) do início de cada semana (segunda-feira).
     *
     * @param from início do intervalo (inclusive)
     * @param to   fim do intervalo (inclusive)
     */
    List<StatsEntryDTO> complianceTrendByWeek(LocalDate from, LocalDate to);
}
