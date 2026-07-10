package com.portal.conecta.checklist.modules.checklist.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.application.usecase.template.query.ChecklistTemplateStatsUseCase;
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
 * Endpoints de agregaÃ§Ã£o para templates de checklist.
 *
 * <p>Base: {@code GET /api/checklist-templates/stats}</p>
 */
@RestController
@RequestMapping("/api/checklist-templates/stats")
@RequiredArgsConstructor
@Tag(name = "Checklist Template Stats", description = "MÃ©tricas de agregaÃ§Ã£o de templates de checklist para dashboards")
public class ChecklistTemplateStatsController {

    private final ChecklistTemplateStatsUseCase statsUseCase;

    @Operation(
            summary = "Agregar templates por dimensÃ£o",
            description = """
                    Retorna contagem de templates agrupados pela dimensÃ£o indicada em `groupBy`.
                    
                    Valores aceitos para `groupBy`:
                    - `status` â€” por status (DRAFT, ACTIVE, INACTIVE)
                    - `active` â€” por flag ativo (true/false)
                    - `day` â€” por dia de criaÃ§Ã£o
                    - `group` â€” nÃºmero de versÃµes por grupo de template
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
            @RequestParam String groupBy
    ) {
        List<StatsEntryDTO> result = switch (groupBy) {
            case "status" -> statsUseCase.countByStatus();
            case "active" -> statsUseCase.countByActive();
            case "day"    -> statsUseCase.countByDay();
            case "group"  -> statsUseCase.countVersionsByGroup();
            default -> throw new InvalidRequestException(
                    "groupBy invÃ¡lido: '" + groupBy + "'. Valores aceitos: status, active, day, group"
            );
        };
        return ResponseEntity.ok(result);
    }
}
