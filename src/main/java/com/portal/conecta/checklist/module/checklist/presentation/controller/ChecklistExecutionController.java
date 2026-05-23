package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.facade.ChecklistExecutionFacade;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.CreateChecklistExecutionUseCase;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecutionMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PatchMapping("/{id}/submit")
    public ResponseEntity<ChecklistExecutionResponseDTO> submitChecklist(@PathVariable UUID id, @RequestBody @Valid ChecklistExecutionSubmitDTO submitDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(checklistExecutionFacade.submitDTO(id,submitDTO));
    }
}
