package com.portal.conecta.checklist.modules.checklist.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.cancel.CancelChecklistExecutionUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.create.CreateChecklistExecutionUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query.ListChecklistHistoryByClassUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query.ListChecklistExecutionsUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query.FindChecklistExecutionByIdUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.submit.SubmitChecklistExecutionUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.update.UpdateChecklistExecutionAnswersUseCase;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistCategory;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.response.ChecklistExecutionHistoryDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.shared.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.portal.conecta.checklist.modules.checklist.presentation.mapper.ChecklistExecutionMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/checklist-executions")
@RequiredArgsConstructor
@Tag(name = "Checklist Executions", description = "Endpoints para gerenciamento de execuções de checklist")
public class ChecklistExecutionController {

    private final CreateChecklistExecutionUseCase createUseCase;
    private final SubmitChecklistExecutionUseCase submitUseCase;
    private final CancelChecklistExecutionUseCase cancelUseCase;
    private final ListChecklistHistoryByClassUseCase listHistoryByClassUseCase;
    private final ListChecklistExecutionsUseCase listExecutionsUseCase;
    private final UpdateChecklistExecutionAnswersUseCase updateAnswersUseCase;
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
                    description = "Dados inválidos no corpo da requisição (campos obrigatórios ausentes ou JSON malformado)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado — token JWT ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para criar execuções de checklist",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Template não encontrado para o ID informado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de estado — o template referenciado não está ativo",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Serviço externo (Hub) ou banco de dados temporariamente indisponível",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado no servidor",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping("/drafts")
    public ResponseEntity<ChecklistExecutionResponseDTO> createDraft(@RequestBody @Valid ChecklistExecutionDraftCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(createUseCase.execute(request.toCommand())));
    }

    @Operation(
            summary = "Submeter execução",
            description = "Finaliza uma execução em DRAFT, registrando todas as respostas e alterando o status para SUBMITTED."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Execução submetida com sucesso",
                    content = @Content(schema = @Schema(implementation = ChecklistExecutionResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos no corpo da requisição (respostas ausentes ou JSON malformado)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado — token JWT ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para submeter esta execução",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Execução não encontrada para o ID informado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de estado — a execução não está no status DRAFT ou foi alterada por outro usuário (optimistic locking)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Serviço externo (Hub) ou banco de dados temporariamente indisponível",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado no servidor",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping("/{executionId}/submit")
    public ResponseEntity<ChecklistExecutionResponseDTO> submit(
            @Parameter(description = "UUID da execução a ser submetida", required = true)
            @PathVariable UUID executionId,
            @RequestBody @Valid ChecklistExecutionSubmitDTO request
    ) {
        return ResponseEntity.ok(mapper.toResponse(submitUseCase.execute(executionId, request.toCommand())));
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
                    responseCode = "400",
                    description = "Formato inválido para o executionId (deve ser um UUID válido)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado — token JWT ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para cancelar esta execução",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Execução não encontrada para o ID informado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de estado — a execução já foi submetida ou cancelada e não pode ser cancelada novamente",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado no servidor",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })

    @PatchMapping("/{executionId}/cancel")
    public ResponseEntity<ChecklistExecutionResponseDTO> cancel(@PathVariable UUID executionId) {
        return ResponseEntity.ok(mapper.toResponse(cancelUseCase.execute(executionId)));
    }

    @Operation(
            summary = "Buscar execução por ID",
            description = "Retorna os detalhes de uma execução de checklist específica pelo seu identificador único."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Execução encontrada com sucesso",
                    content = @Content(schema = @Schema(implementation = ChecklistExecutionResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado — token JWT ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para acessar esta execução",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Execução não encontrada para o ID informado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado no servidor",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/{executionId}")
    public ResponseEntity<ChecklistExecutionResponseDTO> findById(
            @Parameter(description = "UUID da execução a ser buscada", required = true)
            @PathVariable UUID executionId
    ) {
        return ResponseEntity.ok(mapper.toResponse(findByIdUseCase.execute(executionId)));
    }

    @Operation(
            summary = "Listar execuções de checklist",
            description = "Retorna uma lista paginada de execuções de checklist, aplicando os filtros de permissão de acesso conforme o perfil do usuário."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Execuções listadas com sucesso",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado — token JWT ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado no servidor",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping
    public ResponseEntity<Page<ChecklistExecutionResponseDTO>> listAll(
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(value = "category", required = false) ChecklistCategory category
    ) {
        return ResponseEntity.ok(listExecutionsUseCase.execute(pageable, category).map(mapper::toResponse));
    }

    @Operation(
            summary = "Listar histórico de execuções por turma",
            description = "Retorna o histórico de execuções de checklist para uma determinada turma. Filtro opcional por category (grupo de itens da sala: ELETRONICOS, MOVEIS, etc.)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico listado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão para acessar o histórico da turma", content = @Content),
            @ApiResponse(responseCode = "404", description = "Turma não encontrada", content = @Content)
    })
    @GetMapping("/history/class/{classId}")
    public ResponseEntity<Page<ChecklistExecutionHistoryDTO>> listHistoryByClass(
            @PathVariable UUID classId,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(value = "category", required = false) ChecklistCategory category
    ) {
        return ResponseEntity.ok(mapper.toPageHistoryWithEnrichment(
                listHistoryByClassUseCase.execute(classId, pageable, category),
                classId
        ));
    }

    @Operation(
            summary = "Atualizar respostas de um checklist enviado",
            description = "Permite que um usuário autorizado (ex: professor da turma ou administrador) atualize as respostas de uma execução de checklist cujo status seja SUBMITTED. Recalcula a nota de conformidade e gera novas pendências (issues) se houverem itens não conformes."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Respostas atualizadas com sucesso",
                    content = @Content(schema = @Schema(implementation = ChecklistExecutionResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos no corpo da requisição (respostas ausentes ou JSON malformado)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado — token JWT ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para editar esta execução",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Execução não encontrada para o ID informado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de estado — a execução não está no status SUBMITTED",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado no servidor",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PatchMapping("/{executionId}/answers")
    public ResponseEntity<ChecklistExecutionResponseDTO> updateAnswers(
            @Parameter(description = "UUID da execução a ser editada", required = true)
            @PathVariable UUID executionId,
            @RequestBody @Valid ChecklistExecutionSubmitDTO request
    ) {
        return ResponseEntity.ok(mapper.toResponse(updateAnswersUseCase.execute(executionId, request.toCommand())));
    }
}
