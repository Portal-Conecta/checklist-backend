package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.facade.ChecklistExecutionFacade;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.query.ListChecklistHistoryByClassUseCase;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionHistoryDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionResponseDTO;
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
 * Controlador REST responsável pelas operações HTTP de execução de checklists.
 *
 * <p>Expoe endpoints para criar rascunhos, submeter respostas e
 * cancelar execucoes, delegando regras de negocio para a fachada de aplicacao.</p>
 */
@RestController
@RequestMapping("/api/checklist-executions")
@RequiredArgsConstructor
public class ChecklistExecutionController {

    private final ChecklistExecutionFacade checklistExecutionFacade;
    private final ChecklistExecutionMapper checklistExecutionMapper;
    private final ListChecklistHistoryByClassUseCase listHistoryByClassUseCase;


    /**
     * Cria uma execução de checklist em rascunho.
     *
     * @param request payload com turma, sala, período e tipo do checklist.
     * @return resposta HTTP 201 contendo a execução criada.
     */
    @PostMapping("/drafts")
    public ResponseEntity<ChecklistExecutionResponseDTO> createDraft(@RequestBody @Valid ChecklistExecutionDraftCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(checklistExecutionFacade.createDTO(request));
    }

    /**
     * Submete as respostas de uma execução de checklist.
     *
     * @param executionId identificador único da execução.
     * @param request payload com as respostas preenchidas.
     * @return resposta HTTP 200 contendo a execução submetida.
     */
    @PostMapping("/{executionId}/submit")
    public ResponseEntity<ChecklistExecutionResponseDTO> submit(
            @PathVariable UUID executionId,
            @RequestBody @Valid ChecklistExecutionSubmitDTO request
    ) {
        return ResponseEntity.ok(checklistExecutionFacade.submit(executionId, request));
    }

    /**
     * Cancela uma execução de checklist.
     *
     * @param executionId identificador único da execução.
     * @return resposta HTTP 200 contendo a execução cancelada.
     */
    @PatchMapping("/{executionId}/cancel")
    public ResponseEntity<ChecklistExecutionResponseDTO> cancel(
            @PathVariable UUID executionId
    ) {
        return ResponseEntity.ok(checklistExecutionFacade.cancel(executionId));
    }

    /**
     * Lista o histórico de checklists submetidos para uma turma.
     *
     * @param classId identificador único da turma.
     * @return resposta HTTP 200 contendo a lista de registros históricos da turma.
     */
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
    @PatchMapping("/{executionId}/answers")
    public ResponseEntity<ChecklistExecutionResponseDTO> updateAnswers(
            @PathVariable UUID executionId,
            @RequestBody @Valid ChecklistExecutionSubmitDTO request
    ) {
        return ResponseEntity.ok(checklistExecutionFacade.updateAnswers(executionId, request));
    }

}
