package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistTemplateCreateRequest;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistTemplateResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.facade.ChecklistTemplateFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/checklist-templates")
@RequiredArgsConstructor
public class ChecklistTemplateController {

    private final ChecklistTemplateFacade checklistTemplateFacade;

    @PostMapping
    public ResponseEntity<ChecklistTemplateResponseDTO> createTemplate(@RequestBody @Valid ChecklistTemplateCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(checklistTemplateFacade.createTemplate(request));
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<ChecklistTemplateResponseDTO> findTemplateById(@PathVariable UUID templateId) {
        return ResponseEntity.ok(checklistTemplateFacade.findTemplateById(templateId));
    }

    @GetMapping
    public ResponseEntity<List<ChecklistTemplateResponseDTO>> listTemplates() {
        return ResponseEntity.ok(checklistTemplateFacade.listTemplates());
    }
}
