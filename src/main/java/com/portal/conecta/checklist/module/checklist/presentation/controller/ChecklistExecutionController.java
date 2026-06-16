package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.usecase.execution.CancelChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.CreateChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.FindChecklistExecutionByIdUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.ListChecklistHistoryByClassUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.SaveDraftChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.SubmitChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSaveDraftDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionHistoryDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.shared.exception.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controller responsavel por expor os endpoints de gerenciamento das execucoes de checklists.
 *
 * <p>Oferece suporte ao fluxo incremental utilizando cache no Redis para rascunhos
 * e persistencia definitiva no PostgreSQL apos a submissao final.</p>
 */
@RestController
@RequestMapping("/api/checklist-executions")
@RequiredArgsConstructor
@Tag(name = "Checklist Executions", description = "Endpoints para gerenciamento de execuções de checklist")
public class ChecklistExecutionController {

    private final CreateChecklistExecutionUseCase createUseCase;
    private final SubmitChecklistExecutionUseCase submitUseCase;
    private final CancelChecklistExecutionUseCase cancelUseCase;
    private final ListChecklistHistoryByClassUseCase listHistoryByClassUseCase;
    private final SaveDraftChecklistExecutionUseCase saveDraftUseCase;
    private final FindChecklistExecutionByIdUseCase findByIdUseCase;
    private final ChecklistExecutionMapper mapper;

    @Operation(
            summary = "Criar rascunho de execução",
            description = "Inicia uma nova execução de checklist no status DRAFT a partir de um template ativo."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Rascunho de execução criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ChecklistExecutionResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos no corpo da requisição",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado — token JWT ausente ou inválido",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para criar execuções de checklist",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Template não encontrado para o ID informado",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de estado — o template referenciado não está ativo",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado no servidor",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )

    })
    @PostMapping("/drafts")
    public ResponseEntity<ChecklistExecutionResponseDTO> createDraft(@RequestBody @Valid ChecklistExecutionDraftCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(createUseCase.execute(request)));
    }

    @Operation(
            summary = "Buscar execução por ID",
            description = "Recupera os detalhes de uma execução. Caso o status seja DRAFT, aplica de forma transparente a mesclagem com as respostas parciais salvas no Redis."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Execução encontrada e retornada com sucesso",
                        content = @Content(schema = @Schema(implementation = ChecklistExecutionResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado — token JWT ausente ou inválido",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não possui permissão para visualizar esta execução",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Execução não encontrada no banco de dados",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado no servidor",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @GetMapping("/{executionId}")
    public ResponseEntity<ChecklistExecutionResponseDTO> getById(
            @Parameter(description = "UUID da execução do checklist", required = true)
            @PathVariable UUID executionId
    ) {
        return ResponseEntity.ok(findByIdUseCase.execute(executionId));
    }

    @Operation(
            summary = "Submeter execução",
            description = "Finaliza uma execução em DRAFT, consolidando as respostas parciais salvas no Redis e alterando o status para SUBMITTED no PostgreSQL."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Execução submetida com sucesso",
                    content = @Content(schema = @Schema(implementation = ChecklistExecutionResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou itens obrigatórios ausentes no rascunho do Redis",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado — token JWT ausente ou inválido",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para submeter esta execução",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Execução não encontrada",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de estado — a execução não está no status DRAFT",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado no servidor",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @PostMapping("/{executionId}/submit")
    public ResponseEntity<ChecklistExecutionResponseDTO> submit(
            @Parameter(description = "UUID da execução a ser submetida", required = true)
            @PathVariable UUID executionId
    ) {
        return ResponseEntity.ok(mapper.toResponse(submitUseCase.execute(executionId)));
    }

    @Operation(
            summary = "Cancelar execução",
            description = "Cancela uma execução em andamento, alterando seu status para CANCELLED. Execuções já submetidas não podem ser canceladas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Execução cancelada com sucesso",
                        content = @Content(schema = @Schema(implementation = ChecklistExecutionResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para cancelar esta execução",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Execução não encontrada",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de estado — a execução já foi submetida ou cancelada",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado no servidor",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            )
    })
    @PatchMapping("/{executionId}/cancel")
    public ResponseEntity<ChecklistExecutionResponseDTO> cancel(@PathVariable UUID executionId) {
        return ResponseEntity.ok(mapper.toResponse(cancelUseCase.execute(executionId)));
    }

    @GetMapping("/history/class/{classId}")
    public ResponseEntity<Page<ChecklistExecutionHistoryDTO>> listHistoryByClass(
            @PathVariable UUID classId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(mapper.toPageHistory(listHistoryByClassUseCase.execute(classId, pageable)));
    }

    @Operation(
            summary = "Salvar rascunho de execução (Redis)",
            description = """
                    Persiste respostas parciais de forma incremental no Redis (TTL: 4h).
                    Não altera o PostgreSQL — o Postgres é atualizado somente no submit final.
                    Respostas recebidas sobrescrevem as anteriores por itemKey; itens não
                    informados são preservados do estado anterior no Redis.
                    Execuções já submetidas (SUBMITTED) ou canceladas (CANCELED) são rejeitadas.
                    O TTL de 4h é renovado a cada chamada bem-sucedida.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Rascunho salvou no Redis com sucesso"),

            @ApiResponse(
                    responseCode = "400",
                    description = "itemKey inexistente no template ou JSON malformado",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para editar esta execução",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "404",
                    description = "Execução não encontrada no PostgreSQL",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "409",
                    description = "Execução já submetida ou cancelada — não pode ser editada",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),

            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado (incluindo falha de conexão com Redis)",
                        content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PatchMapping("/{executionId}/draft")
    public ResponseEntity<Void> saveDraft(
            @Parameter(description = "UUID da execução a ter o rascunho salvo no Redis", required = true)
            @PathVariable UUID executionId,
            @RequestBody @Valid ChecklistExecutionSaveDraftDTO request
    ) {
        saveDraftUseCase.execute(executionId, request);
        return ResponseEntity.noContent().build();
    }
}