package com.portal.conecta.checklist.modules.checklist.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.CancelChecklistExecutionUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.CreateChecklistExecutionUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query.ListChecklistHistoryByClassUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.SubmitChecklistExecutionUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.command.UpdateChecklistExecutionAnswersUseCase;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.response.ChecklistExecutionHistoryDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.shared.exception.ErrorResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.portal.conecta.checklist.modules.checklist.presentation.mapper.ChecklistExecutionMapper;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubClassProvider;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubRoomProvider;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.modules.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.modules.checklist.domain.valueobject.RoomReference;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/checklist-executions")
@RequiredArgsConstructor
@Tag(name = "Checklist Executions", description = "Endpoints para gerenciamento de execuções de checklist")
public class ChecklistExecutionController {

    private final CreateChecklistExecutionUseCase createUseCase;
    private final SubmitChecklistExecutionUseCase submitUseCase;
    private final CancelChecklistExecutionUseCase cancelUseCase;
    private final ListChecklistHistoryByClassUseCase listHistoryByClassUseCase;
    private final UpdateChecklistExecutionAnswersUseCase updateAnswersUseCase;
    private final ChecklistExecutionMapper mapper;
    private final HubRoomProvider hubRoomProvider;
    private final HubClassProvider hubClassProvider;

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
                    responseCode = "503",
                    description = "Serviço externo (Hub) ou banco de dados temporariamente indisponível",
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
                    description = "Execução não encontrada para o ID informado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de estado — a execução não está no status DRAFT ou foi alterada por outro usuário (optimistic locking)",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Serviço externo (Hub) ou banco de dados temporariamente indisponível",
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
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado — token JWT ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para cancelar esta execução",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Execução não encontrada para o ID informado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de estado — a execução já foi submetida ou cancelada e não pode ser cancelada novamente",
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
        Page<ChecklistExecution> executions = listHistoryByClassUseCase.execute(classId, pageable);
        List<UUID> roomIds = executions.getContent().stream()
                .map(ChecklistExecution::getRoomId)
                .toList();

        Map<UUID, RoomReference> roomMap = Map.of();
        try {
            List<RoomReference> rooms = hubRoomProvider.findByIds(roomIds);
            roomMap = rooms.stream()
                    .collect(Collectors.toMap(RoomReference::getRoomId, room -> room, (r1, r2) -> r1));
        } catch (Exception e) {
            // best-effort enrichment, keep functioning on error
        }

        Map<UUID, ClassReference> classMap = Map.of();
        try {
            List<ClassReference> classes = hubClassProvider.findByIds(List.of(classId));
            classMap = classes.stream()
                    .collect(Collectors.toMap(ClassReference::getClassId, classRef -> classRef, (c1, c2) -> c1));
        } catch (Exception e) {
            // best-effort enrichment, keep functioning on error
        }

        return ResponseEntity.ok(mapper.toPageHistory(executions, roomMap, classMap));
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
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Não autenticado — token JWT ausente ou inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Sem permissão para editar esta execução",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Execução não encontrada para o ID informado",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito de estado — a execução não está no status SUBMITTED",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno inesperado no servidor",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))
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
