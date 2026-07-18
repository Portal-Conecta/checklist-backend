package com.portal.conecta.checklist.modules.checklist.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query.ChecklistExecutionStatsUseCase;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;
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
 * Endpoints de agregação para execuções de checklist.
 *
 * <p>Toda a lógica de GROUP BY é executada no banco — os endpoints devolvem
 * linhas já somadas, prontas para consumo pelo Chart.js.</p>
 *
 * <p>Base: {@code GET /api/checklist-executions/stats}</p>
 */
@RestController
@RequestMapping("/api/checklist-executions/stats")
@RequiredArgsConstructor
@Tag(name = "Checklist Execution Stats", description = "Métricas de agregação de execuções de checklist para dashboards")
public class ChecklistExecutionStatsController {

    private final ChecklistExecutionStatsUseCase statsUseCase;

    @Operation(
            summary = "Agregar execuções por dimensão",
            description = """
                    Retorna contagem de execuções agrupadas pela dimensão indicada em `groupBy`.
                    
                    Valores aceitos para `groupBy`:
                    - `day` — por dia (requer `from`/`to`; padrão: últimos 30 dias)
                    - `status` — por status (DRAFT, SUBMITTED, CANCELED)
                    - `type` — por tipo de checklist
                    - `shift` — por turno
                    - `period` — por período
                    - `day+status` — série temporal por dia **e** status (label = `YYYY-MM-DD|STATUS`)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados de agregação retornados com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StatsEntryDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Parâmetro groupBy inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public ResponseEntity<List<StatsEntryDTO>> aggregate(
            @Parameter(description = "Dimensão de agrupamento", example = "status")
            @RequestParam String groupBy,

            @Parameter(description = "Início do intervalo (YYYY-MM-DD) — usado quando groupBy=day ou day+status")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Fim do intervalo (YYYY-MM-DD) — usado quando groupBy=day ou day+status")
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
                    "groupBy inválido: '" + groupBy + "'. Valores aceitos: day, status, type, shift, period, day+status"
            );
        };
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Taxa de conclusão de execuções",
            description = "Retorna o total de execuções submetidas, o total geral e o percentual de conclusão."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Taxa de conclusão calculada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StatsEntryDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/completion-rate")
    public ResponseEntity<List<StatsEntryDTO>> completionRate() {
        return ResponseEntity.ok(statsUseCase.completionRate());
    }

    @Operation(
            summary = "Tempo médio de preenchimento por dia",
            description = "Retorna a média de segundos entre início e submissão, por dia. " +
                    "Somente execuções com submitted_at preenchido são consideradas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tempo médio por dia calculado com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StatsEntryDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/avg-fill-time")
    public ResponseEntity<List<StatsEntryDTO>> avgFillTime(
            @Parameter(description = "Início do intervalo (YYYY-MM-DD); padrão: 30 dias atrás")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Fim do intervalo (YYYY-MM-DD); padrão: hoje")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(statsUseCase.avgFillTimeByDay(from, to));
    }

    @Operation(
            summary = "Percentual de execuções com ao menos uma não-conformidade",
            description = "Retorna execuções com issue, total de execuções submetidas e o percentual."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Taxa calculada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StatsEntryDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/with-issues-rate")
    public ResponseEntity<List<StatsEntryDTO>> withIssuesRate() {
        return ResponseEntity.ok(statsUseCase.withIssuesRate());
    }

    @Operation(
            summary = "Heatmap de execuções por turno × dia da semana",
            description = "Retorna células do heatmap com turno, dia da semana (0=dom … 6=sáb) e contagem."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Heatmap calculado com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StatsEntryDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/heatmap")
    public ResponseEntity<List<StatsEntryDTO>> heatmap() {
        return ResponseEntity.ok(statsUseCase.heatmap());
    }

    /**
     * Valida o intervalo de datas informado pelo cliente.
     *
     * <p>Regras aplicadas:
     * <ul>
     *   <li>{@code from} deve ser anterior ou igual a {@code to}</li>
     *   <li>intervalo máximo de 2 anos</li>
     *   <li>datas não podem estar no futuro (métricas são históricas)</li>
     * </ul>
     * </p>
     *
     * @param from início do intervalo (pode ser {@code null})
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
                    "Intervalo de data não pode exceder 2 anos. Solicitado: " +
                    ChronoUnit.YEARS.between(from, to) + " anos"
            );
        }

        LocalDate hoje = LocalDate.now();
        if (from.isAfter(hoje) || to.isAfter(hoje)) {
            throw new InvalidRequestException(
                    "Datas não podem ser no futuro. " +
                    "from: " + from + " (máx: " + hoje + "), " +
                    "to: " + to + " (máx: " + hoje + ")"
            );
        }
    }
}
