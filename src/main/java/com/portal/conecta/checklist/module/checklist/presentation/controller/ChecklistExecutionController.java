package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.usecase.execution.CancelChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.CreateChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.ListChecklistHistoryByClassUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.SubmitChecklistExecutionUseCase;
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

@RestController
@RequestMapping("/api/checklist-executions")
@RequiredArgsConstructor
public class ChecklistExecutionController {

    private final CreateChecklistExecutionUseCase createUseCase;
    private final SubmitChecklistExecutionUseCase submitUseCase;
    private final CancelChecklistExecutionUseCase cancelUseCase;
    private final ListChecklistHistoryByClassUseCase listHistoryByClassUseCase;
    private final ChecklistExecutionMapper mapper;

    @PostMapping("/drafts")
    public ResponseEntity<ChecklistExecutionResponseDTO> createDraft(@RequestBody @Valid ChecklistExecutionDraftCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(createUseCase.execute(request)));
    }

    @PostMapping("/{executionId}/submit")
    public ResponseEntity<ChecklistExecutionResponseDTO> submit(
            @PathVariable UUID executionId,
            @RequestBody @Valid ChecklistExecutionSubmitDTO request
    ) {
        return ResponseEntity.ok(mapper.toResponse(submitUseCase.execute(executionId, request)));
    }

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
}
