package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.facade.ChecklistExecutionFacade;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionSubmitDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping("/search")
    public ResponseEntity<List<ChecklistExecutionResponseDTO>> search(
            @RequestParam String query
    ) {
        return ResponseEntity.ok(checklistExecutionFacade.search(query));
    }
}
