package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.usecase.template.CreateChecklistTemplateUseCase;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistTemplateCreateRequest;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistTemplateResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistTemplateMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checklist-templates")
@RequiredArgsConstructor
public class ChecklistTemplateController {

    private final CreateChecklistTemplateUseCase createChecklistTemplateUseCase;
    private final ChecklistTemplateMapper checklistTemplateMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChecklistTemplateResponseDTO createTemplate(@RequestBody @Valid ChecklistTemplateCreateRequest request) {
        var template = createChecklistTemplateUseCase.execute(request);
        return checklistTemplateMapper.toResponse(template);
    }
}
