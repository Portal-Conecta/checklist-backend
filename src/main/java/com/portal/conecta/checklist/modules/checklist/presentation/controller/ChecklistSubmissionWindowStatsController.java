package com.portal.conecta.checklist.modules.checklist.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.application.usecase.window.query.ChecklistSubmissionWindowStatsUseCase;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.AvgFillTimeEntryDTO;
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
 * Endpoints de agregaÃ§Ã£o para janelas de submissÃ£o de checklist.
 *
 * <p>Base: {@code GET /api/submission-windows/stats}</p>
 */
@RestController
@RequestMapping("/api/submission-windows/stats")
@RequiredArgsConstructor
@Tag(name = "Submission Window Stats", description = "MÃ©tricas de agregaÃ§Ã£o de janelas de submissÃ£o para dashboards")
public class ChecklistSubmissionWindowStatsController {

    private final ChecklistSubmissionWindowStatsUseCase statsUseCase;

    @Operation(
            summary = "Agregar janelas de submissÃ£o por dimensÃ£o",
            description = """
                    Retorna contagem de janelas agrupadas pela dimensÃ£o indicada em `groupBy`.
                    
                    Valores aceitos para `groupBy`:
                    - `type` â€” por tipo de checklist
                    - `shift` â€” por turno
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
            @Parameter(description = "DimensÃ£o de agrupamento", example = "type")
            @RequestParam String groupBy
    ) {
        List<StatsEntryDTO> result = switch (groupBy) {
            case "type"  -> statsUseCase.countByType();
            case "shift" -> statsUseCase.countByShift();
            default -> throw new InvalidRequestException(
                    "groupBy invÃ¡lido: '" + groupBy + "'. Valores aceitos: type, shift"
            );
        };
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "DuraÃ§Ã£o mÃ©dia por tipo de checklist",
            description = "Retorna a mÃ©dia de `duration_minutes` por tipo de checklist das janelas de submissÃ£o."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "DuraÃ§Ã£o mÃ©dia calculada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AvgFillTimeEntryDTO.class)))),
            @ApiResponse(responseCode = "401", description = "NÃ£o autenticado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/avg-duration")
    public ResponseEntity<List<AvgFillTimeEntryDTO>> avgDuration() {
        return ResponseEntity.ok(statsUseCase.avgDurationByType());
    }
}
