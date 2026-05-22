package com.portal.conecta.checklist.module.checklist.presentation.facade;

import com.portal.conecta.checklist.module.checklist.application.usecase.template.ActivateChecklistTemplateUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.CreateChecklistTemplateUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.FindChecklistTemplateByIdUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.ListChecklistTemplatesUseCase;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistTemplateCreateRequest;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistTemplateResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChecklistTemplateFacade {

    private final ActivateChecklistTemplateUseCase activateChecklistTemplateUseCase;
    private final CreateChecklistTemplateUseCase createChecklistTemplateUseCase;
    private final FindChecklistTemplateByIdUseCase findChecklistTemplateByIdUseCase;
    private final ListChecklistTemplatesUseCase listChecklistTemplatesUseCase;
    private final ChecklistTemplateMapper checklistTemplateMapper;

    public ChecklistTemplateResponseDTO createTemplate(ChecklistTemplateCreateRequest request) {
        var template = createChecklistTemplateUseCase.execute(request);
        return checklistTemplateMapper.toResponse(template);
    }

    public ChecklistTemplateResponseDTO activateTemplate(UUID templateId) {
        var template = activateChecklistTemplateUseCase.execute(templateId);
        return checklistTemplateMapper.toResponse(template);
    }

    public ChecklistTemplateResponseDTO findTemplateById(UUID templateId) {
        var template = findChecklistTemplateByIdUseCase.execute(templateId);
        return checklistTemplateMapper.toResponse(template);
    }

    public List<ChecklistTemplateResponseDTO> listTemplates() {
        return checklistTemplateMapper.toResponseList(listChecklistTemplatesUseCase.execute());
    }
}
