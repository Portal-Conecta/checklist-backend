package com.portal.conecta.checklist.modules.checklist.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query.ChecklistExecutionStatsUseCase;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.AvgFillTimeEntryDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.CompletionRateDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.HeatmapEntryDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.WithIssuesRateDTO;
import com.portal.conecta.checklist.shared.exception.ApiError;
import com.portal.conecta.checklist.shared.exception.InvalidRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Endpoints de agregaÃ§Ã£o para execuÃ§Ãµes de checklist.
 *
 * <p>Toda a lÃ³gica de GROUP BY Ã© executada no banco â€” os endpoints devolvem
 * linhas jÃ¡ somadas, prontas para consumo pelo Chart.js.</p>
 *
 * <p>Base: {@code GET /api/checklist-executions/stats}</p>
 */
@RestController
@RequestMapping("/api/checklist-executions/stats")
@RequiredArgsConstructor
@Tag(name = "Checklist Execution Stats", description = "MÃ©tricas de agregaÃ§Ã£o de execuÃ§Ãµes de checklist para dashboards")
public class ChecklistExecutionStatsController {

    private final ChecklistExecutionStatsUseCase statsUseCase;

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // GenÃ©rico por groupBy
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Operation(
            summary = "Agregar execuÃ§Ãµes por dimensÃ£o",
            description = """
                    Retorna contagem de execuÃ§Ãµes agrupadas pela dimensÃ£o indicada em `groupBy`.
                    
                    Valores aceitos para `groupBy`:
                    - `day` â€” por dia (requer `from`/`to`; padrÃ£o: Ãºltimos 30 dias)
                    - `status` â€” por status (DRAFT, SUBMITTED, CANCELED)
                    - `type` â€” por tipo de checklist
                    - `shift` â€” por turno
                    - `period` â€” por perÃ­odo
                    - `day+status` â€” sÃ©rie temporal por dia **e** status (label = `YYYY-MM-DD|STATUS`)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados de agregaÃ§Ã£o retornados com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StatsEntryDTO.class)))),
            @ApiResponse(responseCode = "400", description = "ParÃ¢metro groupBy invÃ¡lido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public ResponseEntity<List<StatsEntryDTO>> aggregate(
            @Parameter(description = "DimensÃ£o de agrupamento", example = "status")
            @RequestParam String groupBy,

            @Parameter(description = "InÃ­cio do intervalo (YYYY-MM-DD) â€” usado quando groupBy=day ou day+status")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Fim do intervalo (YYYY-MM-DD) â€” usado quando groupBy=day ou day+status")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        validateDateRange(from, to);

        List<StatsEntryDTO> result = switch (groupBy) {
            case "day"        -> statsUseCase.countByDay(from, to);
            case "status"     -> statsUseCase.countByStatus();
            case "type"       -> statsUseCase.countByType();
            case "shift"      -> statsUseCase.countByShift();
            case "period"     -> statsUseCase.countByPeriod();
            case "day+status" -> statsUseCase.countByDayAndStatus(from, to);
            default -> throw new InvalidRequestException(
                    "groupBy invÃ¡lido: '" + groupBy + "'. Valores aceitos: day, status, type, shift, period, day+status"
            );
        };
        return ResponseEntity.ok(result);
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Sub-recursos fixos
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Operation(
            summary = "Taxa de conclusÃ£o de execuÃ§Ãµes",
            description = "Retorna o total de execuÃ§Ãµes submetidas, o total geral e o percentual de conclusÃ£o."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Taxa de conclusÃ£o calculada com sucesso",
                    content = @Content(schema = @Schema(implementation = CompletionRateDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/completion-rate")
    public ResponseEntity<CompletionRateDTO> completionRate() {
        return ResponseEntity.ok(statsUseCase.completionRate());
    }

    @Operation(
            summary = "Tempo mÃ©dio de preenchimento por dia",
            description = "Retorna a mÃ©dia de segundos entre inÃ­cio e submissÃ£o, por dia. " +
                    "Somente execuÃ§Ãµes com submitted_at preenchido sÃ£o consideradas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tempo mÃ©dio por dia calculado com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AvgFillTimeEntryDTO.class)))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/avg-fill-time")
    public ResponseEntity<List<AvgFillTimeEntryDTO>> avgFillTime(
            @Parameter(description = "InÃ­cio do intervalo (YYYY-MM-DD); padrÃ£o: 30 dias atrÃ¡s")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Fim do intervalo (YYYY-MM-DD); padrÃ£o: hoje")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(statsUseCase.avgFillTimeByDay(from, to));
    }

    @Operation(
            summary = "Percentual de execuÃ§Ãµes com ao menos uma nÃ£o-conformidade",
            description = "Retorna execuÃ§Ãµes com issue, total de execuÃ§Ãµes submetidas e o percentual."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Taxa calculada com sucesso",
                    content = @Content(schema = @Schema(implementation = WithIssuesRateDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/with-issues-rate")
    public ResponseEntity<WithIssuesRateDTO> withIssuesRate() {
        return ResponseEntity.ok(statsUseCase.withIssuesRate());
    }

    @Operation(
            summary = "Heatmap de execuÃ§Ãµes por turno Ã— dia da semana",
            description = "Retorna cÃ©lulas do heatmap com turno, dia da semana (0=dom â€¦ 6=sÃ¡b) e contagem."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Heatmap calculado com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = HeatmapEntryDTO.class)))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/heatmap")
    public ResponseEntity<List<HeatmapEntryDTO>> heatmap() {
        return ResponseEntity.ok(statsUseCase.heatmap());
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // ValidaÃ§Ãµes auxiliares
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Valida o intervalo de datas informado pelo cliente.
     *
     * <p>Regras aplicadas:
     * <ul>
     *   <li>{@code from} deve ser anterior ou igual a {@code to}</li>
     *   <li>intervalo mÃ¡ximo de 2 anos</li>
     *   <li>datas nÃ£o podem estar no futuro (mÃ©tricas sÃ£o histÃ³ricas)</li>
     * </ul>
     * </p>
     *
     * @param from inÃ­cio do intervalo (pode ser {@code null})
     * @param to   fim do intervalo (pode ser {@code null})
     * @throws InvalidRequestException se alguma regra for violada
     */
    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) return;

        if (from.isAfter(to)) {
            throw new InvalidRequestException(
                    "'from' (" + from + ") deve ser anterior ou igual a 'to' (" + to + ")"
            );
        }

        if (ChronoUnit.YEARS.between(from, to) > 2) {
            throw new InvalidRequestException(
                    "Intervalo de data nÃ£o pode exceder 2 anos. Solicitado: " +
                    ChronoUnit.YEARS.between(from, to) + " anos"
            );
        }

        LocalDate hoje = LocalDate.now();
        if (from.isAfter(hoje) || to.isAfter(hoje)) {
            throw new InvalidRequestException(
                    "Datas nÃ£o podem ser no futuro. " +
                    "from: " + from + " (mÃ¡x: " + hoje + "), " +
                    "to: " + to + " (mÃ¡x: " + hoje + ")"
            );
        }
    }
}
