package com.portal.conecta.checklist.modules.checklist.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.application.usecase.window.query.ChecklistSubmissionWindowStatsUseCase;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de agregação para janelas de submissão de checklist.
 *
 * <p>Base: {@code GET /api/submission-windows/stats}</p>
 */
@RestController
@RequestMapping("/api/submission-windows/stats")
@RequiredArgsConstructor
@Tag(name = "Submission Window Stats", description = "Métricas de agregação de janelas de submissão para dashboards")
public class ChecklistSubmissionWindowStatsController {

    private final ChecklistSubmissionWindowStatsUseCase statsUseCase;

    @Operation(
            summary = "Agregar janelas de submissão por dimensão",
            description = """
                    Retorna contagem de janelas agrupadas pela dimensão indicada em `groupBy`.
                    
                    Valores aceitos para `groupBy`:
                    - `type` — por tipo de checklist
                    - `shift` — por turno
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
            @Parameter(description = "Dimensão de agrupamento", example = "type")
            @RequestParam String groupBy
    ) {
        List<StatsEntryDTO> result = switch (groupBy) {
            case "type"  -> statsUseCase.countByType();
            case "shift" -> statsUseCase.countByShift();
            default -> throw new InvalidRequestException(
                    "groupBy inválido: '" + groupBy + "'. Valores aceitos: type, shift"
            );
        };
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Duração média por tipo de checklist",
            description = "Retorna a média de `duration_minutes` por tipo de checklist das janelas de submissão."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Duração média calculada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StatsEntryDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/avg-duration")
    public ResponseEntity<List<StatsEntryDTO>> avgDuration() {
        return ResponseEntity.ok(statsUseCase.avgDurationByType());
    }
}
