package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.usecase.template.*;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistTemplateCreateRequest;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistTemplateResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.update.ChecklistTemplateEditRequest;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistTemplateMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/checklist-templates")
@RequiredArgsConstructor
public class ChecklistTemplateController {

    private final CreateChecklistTemplateUseCase createUseCase;
    private final ActivateChecklistTemplateUseCase activateUseCase;
    private final FindChecklistTemplateByIdUseCase findByIdUseCase;
    private final ListChecklistTemplatesUseCase listUseCase;
    private final EditChecklistTemplateUseCase editUseCase;
    private final ChecklistTemplateMapper mapper;

    @PostMapping
    public ResponseEntity<ChecklistTemplateResponseDTO> createTemplate(@RequestBody @Valid ChecklistTemplateCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(createUseCase.execute(request)));
    }

    @PatchMapping("/{templateId}/activate")
    public ResponseEntity<ChecklistTemplateResponseDTO> activateTemplate(@PathVariable UUID templateId) {
        return ResponseEntity.ok(mapper.toResponse(activateUseCase.execute(templateId)));
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<ChecklistTemplateResponseDTO> findTemplateById(@PathVariable UUID templateId) {
        return ResponseEntity.ok(mapper.toResponse(findByIdUseCase.execute(templateId)));
    }

    @GetMapping
    public ResponseEntity<List<ChecklistTemplateResponseDTO>> listTemplates() {
        return ResponseEntity.ok(mapper.toResponseList(listUseCase.execute()));
    }

    @PatchMapping("/{templateId}")
    public ResponseEntity<ChecklistTemplateResponseDTO> editTemplate(@PathVariable UUID templateId, @RequestBody @Valid ChecklistTemplateEditRequest request){
        return ResponseEntity.ok(mapper.toResponse(editUseCase.execute(templateId, request)));
    }
}
