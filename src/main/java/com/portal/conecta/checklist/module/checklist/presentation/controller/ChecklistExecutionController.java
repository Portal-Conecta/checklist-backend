package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.facade.ChecklistExecutionFacade;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.update.UpdateChecklistExecutionAnswersUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.query.ListChecklistHistoryByClassUseCase;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionHistoryDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
public class ChecklistExecutionController {

    private final ChecklistExecutionFacade checklistExecutionFacade;
    private final ChecklistExecutionMapper checklistExecutionMapper;
    private final ListChecklistHistoryByClassUseCase listHistoryByClassUseCase;
    private  final UpdateChecklistExecutionAnswersUseCase updateChecklistExecutionAnswersUseCase;

    @PostMapping("/drafts")
    public ResponseEntity<ChecklistExecutionResponseDTO> createDraft(@RequestBody @Valid ChecklistExecutionDraftCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(checklistExecutionFacade.createDTO(request));
    }

    @PostMapping("/{executionId}/submit")
    public ResponseEntity<ChecklistExecutionResponseDTO> submit(
            @PathVariable UUID executionId,
            @RequestBody @Valid ChecklistExecutionSubmitDTO request
    ) {
        return ResponseEntity.ok(checklistExecutionFacade.submit(executionId, request));
    }

    @PatchMapping("/{executionId}/cancel")
    public ResponseEntity<ChecklistExecutionResponseDTO> cancel(
            @PathVariable UUID executionId
    ) {
        return ResponseEntity.ok(checklistExecutionFacade.cancel(executionId));
    }

    @GetMapping("/history/class/{classId}")
    public ResponseEntity<Page<ChecklistExecutionHistoryDTO>> listHistoryByClass(
            @PathVariable UUID classId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                checklistExecutionMapper.toPageHistory(
                        listHistoryByClassUseCase.execute(classId, pageable)
                )
        );
    }

    @Operation(
            summary = "Atualiza as respostas de um checklist enviado",
            description = "Permite que um usuário autorizado (ex: professor da turma ou administrador) atualize as respostas de uma execução de checklist cujo status seja SUBMITTED. Recalcula a nota de conformidade e gera novas pendências (issues) se houverem itens não conformes.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Respostas atualizadas com sucesso.",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChecklistExecutionResponseDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Requisição inválida ou o status atual do checklist não permite edição (deve ser SUBMITTED).",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Usuário não possui permissão/perfil para gerenciar esta execução ou turma.",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Execução de checklist não encontrada para o UUID fornecido.",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )

    @PatchMapping("/{executionId}/answers")
    public ResponseEntity<ChecklistExecutionResponseDTO> updateAnswers(
            @PathVariable UUID executionId,
            @RequestBody @Valid ChecklistExecutionSubmitDTO request
    ) {
        var execution = updateChecklistExecutionAnswersUseCase.execute(executionId, request);
        return ResponseEntity.ok(checklistExecutionMapper.toResponse(execution));
    }
}