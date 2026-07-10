package com.portal.conecta.checklist.modules.checklist.issues.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.query.ChecklistIssueStatsUseCase;
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
 * Endpoints de agregação para não-conformidades (issues).
 *
 * <p>Toda a lógica de GROUP BY é executada no banco — os endpoints devolvem
 * linhas já somadas, prontas para consumo pelo Chart.js.</p>
 *
 * <p>Base: {@code GET /api/checklist-issues/stats}</p>
 */
@RestController
@RequestMapping("/api/checklist-issues/stats")
@RequiredArgsConstructor
@Tag(name = "Checklist Issue Stats", description = "Métricas de agregação de não-conformidades para dashboards")
public class ChecklistIssueStatsController {

    private final ChecklistIssueStatsUseCase statsUseCase;

    // ────────────────────────────────────────────────────────────────────────
    // Genérico por groupBy
    // ────────────────────────────────────────────────────────────────────────

    @Operation(
            summary = "Agregar issues por dimensão",
            description = """
                    Retorna contagem de issues agrupadas pela dimensão indicada em `groupBy`.
                    
                    Valores aceitos para `groupBy`:
                    - `day` — por dia de criação (requer `from`/`to`; padrão: últimos 30 dias)
                    - `status` — por status da issue
                    - `priority` — por prioridade
                    - `type` — por tipo de checklist da execução vinculada
                    - `item` — top itens que mais falham (use `limit` para controlar; padrão: 10)
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

            @Parameter(description = "Início do intervalo (YYYY-MM-DD) — usado quando groupBy=day")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @Parameter(description = "Fim do intervalo (YYYY-MM-DD) — usado quando groupBy=day")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @Parameter(description = "Número de itens a retornar — usado quando groupBy=item; padrão: 10; máximo: 100")
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
                    "groupBy inválido: '" + groupBy + "'. Valores aceitos: day, status, priority, type, item"
            );
        };
        return ResponseEntity.ok(result);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Sub-recursos fixos
    // ────────────────────────────────────────────────────────────────────────

    @Operation(
            summary = "Divisão entre issues abertas e resolvidas",
            description = "Retorna contagem de issues com e sem data de resolução, mais o total."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Divisão calculada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StatsEntryDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/resolution-split")
    public ResponseEntity<List<StatsEntryDTO>> resolutionSplit() {
        return ResponseEntity.ok(statsUseCase.resolutionSplit());
    }

    @Operation(
            summary = "Taxa de resolução de issues",
            description = "Retorna issues resolvidas, total e percentual de resolução."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Taxa de resolução calculada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StatsEntryDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/resolution-rate")
    public ResponseEntity<List<StatsEntryDTO>> resolutionRate() {
        return ResponseEntity.ok(statsUseCase.resolutionRate());
    }

    @Operation(
            summary = "Tempo médio de resolução de issues",
            description = "Retorna a média em segundos de (resolved_at - due_at) para issues resolvidas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tempo médio calculado com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StatsEntryDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/avg-resolution-time")
    public ResponseEntity<List<StatsEntryDTO>> avgResolutionTime() {
        return ResponseEntity.ok(statsUseCase.avgResolutionTime());
    }

    @Operation(
            summary = "Issues vencidas e não resolvidas",
            description = "Retorna o total de issues cujo prazo (due_at) já passou e que ainda não foram resolvidas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total de issues vencidas calculado com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StatsEntryDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/overdue")
    public ResponseEntity<List<StatsEntryDTO>> overdue() {
        return ResponseEntity.ok(statsUseCase.overdueCount());
    }

    @Operation(
            summary = "Média de não-conformidades por execução",
            description = "Retorna o total de issues, total de execuções com issues e a média."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Média calculada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StatsEntryDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/per-execution")
    public ResponseEntity<List<StatsEntryDTO>> perExecution() {
        return ResponseEntity.ok(statsUseCase.issuesPerExecution());
    }

    // ────────────────────────────────────────────────────────────────────────
    // Validações auxiliares
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Valida o intervalo de datas informado pelo cliente.
     *
     * <p>Regras aplicadas:
     * <ul>
     *   <li>{@code from} deve ser anterior ou igual a {@code to}</li>
     *   <li>intervalo máximo de 2 anos</li>
     *   <li>datas não podem estar no futuro</li>
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

    /**
     * Valida o parâmetro {@code limit} usado no top de itens.
     *
     * @param limit número de itens (pode ser {@code null})
     * @throws InvalidRequestException se {@code limit < 1}
     */
    private void validateLimit(Integer limit) {
        if (limit != null && limit < 1) {
            throw new InvalidRequestException(
                    "'limit' deve ser no mínimo 1. Valor informado: " + limit
            );
        }
    }
}
