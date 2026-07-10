package com.portal.conecta.checklist.modules.checklist.issues.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.query.ChecklistIssueStatsUseCase;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.AvgResolutionTimeDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.IssuesPerExecutionDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.OverdueIssuesDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.ResolutionRateDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.ResolutionSplitDTO;
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
 * Endpoints de agregaÃ§Ã£o para nÃ£o-conformidades (issues).
 *
 * <p>Toda a lÃ³gica de GROUP BY Ã© executada no banco â€” os endpoints devolvem
 * linhas jÃ¡ somadas, prontas para consumo pelo Chart.js.</p>
 *
 * <p>Base: {@code GET /api/checklist-issues/stats}</p>
 */
@RestController
@RequestMapping("/api/checklist-issues/stats")
@RequiredArgsConstructor
@Tag(name = "Checklist Issue Stats", description = "MÃ©tricas de agregaÃ§Ã£o de nÃ£o-conformidades para dashboards")
public class ChecklistIssueStatsController {

    private final ChecklistIssueStatsUseCase statsUseCase;

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // GenÃ©rico por groupBy
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Operation(
            summary = "Agregar issues por dimensÃ£o",
            description = """
                    Retorna contagem de issues agrupadas pela dimensÃ£o indicada em `groupBy`.
                    
                    Valores aceitos para `groupBy`:
                    - `day` â€” por dia de criaÃ§Ã£o (requer `from`/`to`; padrÃ£o: Ãºltimos 30 dias)
                    - `status` â€” por status da issue
                    - `priority` â€” por prioridade
                    - `type` â€” por tipo de checklist da execuÃ§Ã£o vinculada
                    - `item` â€” top itens que mais falham (use `limit` para controlar; padrÃ£o: 10)
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

            @Parameter(description = "InÃ­cio do intervalo (YYYY-MM-DD) â€” usado quando groupBy=day")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Fim do intervalo (YYYY-MM-DD) â€” usado quando groupBy=day")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @Parameter(description = "NÃºmero de itens a retornar â€” usado quando groupBy=item; padrÃ£o: 10; mÃ¡ximo: 100")
            @RequestParam(required = false) Integer limit
    ) {
        validateDateRange(from, to);
        validateLimit(limit);

        List<StatsEntryDTO> result = switch (groupBy) {
            case "day"      -> statsUseCase.countByDay(from, to);
            case "status"   -> statsUseCase.countByStatus();
            case "priority" -> statsUseCase.countByPriority();
            case "type"     -> statsUseCase.countByChecklistType();
            case "item"     -> statsUseCase.topFailingItems(limit);
            default -> throw new InvalidRequestException(
                    "groupBy invÃ¡lido: '" + groupBy + "'. Valores aceitos: day, status, priority, type, item"
            );
        };
        return ResponseEntity.ok(result);
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Sub-recursos fixos
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Operation(
            summary = "DivisÃ£o entre issues abertas e resolvidas",
            description = "Retorna contagem de issues com e sem data de resoluÃ§Ã£o, mais o total."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DivisÃ£o calculada com sucesso",
                    content = @Content(schema = @Schema(implementation = ResolutionSplitDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/resolution-split")
    public ResponseEntity<ResolutionSplitDTO> resolutionSplit() {
        return ResponseEntity.ok(statsUseCase.resolutionSplit());
    }

    @Operation(
            summary = "Taxa de resoluÃ§Ã£o de issues",
            description = "Retorna issues resolvidas, total e percentual de resoluÃ§Ã£o."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Taxa de resoluÃ§Ã£o calculada com sucesso",
                    content = @Content(schema = @Schema(implementation = ResolutionRateDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/resolution-rate")
    public ResponseEntity<ResolutionRateDTO> resolutionRate() {
        return ResponseEntity.ok(statsUseCase.resolutionRate());
    }

    @Operation(
            summary = "Tempo mÃ©dio de resoluÃ§Ã£o de issues",
            description = "Retorna a mÃ©dia em segundos de (resolved_at - due_at) para issues resolvidas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tempo mÃ©dio calculado com sucesso",
                    content = @Content(schema = @Schema(implementation = AvgResolutionTimeDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/avg-resolution-time")
    public ResponseEntity<AvgResolutionTimeDTO> avgResolutionTime() {
        return ResponseEntity.ok(statsUseCase.avgResolutionTime());
    }

    @Operation(
            summary = "Issues vencidas e nÃ£o resolvidas",
            description = "Retorna o total de issues cujo prazo (due_at) jÃ¡ passou e que ainda nÃ£o foram resolvidas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total de issues vencidas calculado com sucesso",
                    content = @Content(schema = @Schema(implementation = OverdueIssuesDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/overdue")
    public ResponseEntity<OverdueIssuesDTO> overdue() {
        return ResponseEntity.ok(statsUseCase.overdueCount());
    }

    @Operation(
            summary = "MÃ©dia de nÃ£o-conformidades por execuÃ§Ã£o",
            description = "Retorna o total de issues, total de execuÃ§Ãµes com issues e a mÃ©dia."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "MÃ©dia calculada com sucesso",
                    content = @Content(schema = @Schema(implementation = IssuesPerExecutionDTO.class))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/per-execution")
    public ResponseEntity<IssuesPerExecutionDTO> perExecution() {
        return ResponseEntity.ok(statsUseCase.issuesPerExecution());
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
     *   <li>datas nÃ£o podem estar no futuro</li>
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

    /**
     * Valida o parÃ¢metro {@code limit} usado no top de itens.
     *
     * @param limit nÃºmero de itens (pode ser {@code null})
     * @throws InvalidRequestException se {@code limit < 1}
     */
    private void validateLimit(Integer limit) {
        if (limit != null && limit < 1) {
            throw new InvalidRequestException(
                    "'limit' deve ser no mÃ­nimo 1. Valor informado: " + limit
            );
        }
    }
}
