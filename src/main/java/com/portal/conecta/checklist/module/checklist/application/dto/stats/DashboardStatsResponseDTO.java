package com.portal.conecta.checklist.module.checklist.application.dto.stats;

import com.portal.conecta.checklist.shared.stats.StatsEntryDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/**
 * Resposta composta do dashboard: reune, numa unica payload, todos os graficos
 * fixos do painel de checklist. Reduz N requisicoes (uma por grafico) para uma.
 *
 * <p>Cada bloco reaproveita o formato generico {@link StatsEntryDTO} ({label,
 * value}) consumido pelo Chart.js. Vive na camada application porque o use case
 * ({@code ChecklistDashboardUseCase}) retorna esse tipo diretamente, e movê-lo
 * para presentation violaria a regra de arquitetura "application nao depende de
 * presentation".</p>
 */
@Schema(description = "Payload unica com todos os graficos fixos do dashboard de checklist")
public record DashboardStatsResponseDTO(

        @Schema(description = "Periodo efetivo considerado (apos aplicar os defaults)")
        Periodo periodo,

        @Schema(description = "Execucoes por dia no periodo (serie temporal)")
        List<StatsEntryDTO> execucoesPorDia,

        @Schema(description = "Execucoes agrupadas por status")
        List<StatsEntryDTO> execucoesPorStatus,

        @Schema(description = "Taxa de conclusao (submetidas vs total)")
        List<StatsEntryDTO> taxaConclusao,

        @Schema(description = "Nao-conformidades (issues) agrupadas por status")
        List<StatsEntryDTO> issuesPorStatus,

        @Schema(description = "Nao-conformidades (issues) agrupadas por prioridade")
        List<StatsEntryDTO> issuesPorPrioridade,

        @Schema(description = "Nao-conformidades (issues) por dia no periodo")
        List<StatsEntryDTO> issuesPorDia,

        @Schema(description = "Execucoes submetidas por turno e faixa de compliance (label = SHIFT|ok|atencao|critico)")
        List<StatsEntryDTO> performancePorTurno,

        @Schema(description = "Media de compliance_score por semana no periodo (label = data ISO de inicio da semana)")
        List<StatsEntryDTO> tendenciaConformidade
) {

    @Schema(description = "Intervalo de datas efetivamente usado nas agregacoes temporais")
    public record Periodo(LocalDate from, LocalDate to) {
    }
}
