package com.portal.conecta.checklist.module.checklist.application.facade;

import com.portal.conecta.checklist.module.checklist.application.usecase.template.command.ActivateChecklistTemplateUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.command.CreateChecklistTemplateUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.query.FindChecklistTemplateByIdUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.query.ListChecklistTemplatesUseCase;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistTemplateCreateRequest;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistTemplateResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Fachada de aplicacao para operacoes de templates de checklist.
 *
 * <p>Centraliza a chamada dos use cases e converte entidades de dominio para
 * DTOs de resposta usados pela camada de apresentacao.</p>
 */
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
