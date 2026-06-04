package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.facade.ChecklistExecutionFacade;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistTemplateResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/checklist-executions")
@RequiredArgsConstructor
public class ChecklistExecutionController {

    private final ChecklistExecutionFacade checklistExecutionFacade;


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
    public ResponseEntity<ChecklistExecutionResponseDTO>cancel(
            @PathVariable UUID executionId
    ){
        return ResponseEntity.ok(checklistExecutionFacade.cancel(executionId));
    }
    @GetMapping("/{executionId}")
    public ResponseEntity<ChecklistExecutionResponseDTO> findExecutionById(@PathVariable UUID executionId) {
        return ResponseEntity.ok(checklistExecutionFacade.findExecutionById(executionId));
    }

    @GetMapping
    public ResponseEntity<Page<ChecklistExecutionResponseDTO>> listExecution(Pageable pageable){
        return ResponseEntity.ok(checklistExecutionFacade.listExecution(pageable));
    }
}
