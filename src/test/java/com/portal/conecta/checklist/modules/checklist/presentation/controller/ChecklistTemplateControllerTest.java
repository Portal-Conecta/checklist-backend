package com.portal.conecta.checklist.modules.checklist.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.application.usecase.template.command.ActivateChecklistTemplateUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.template.command.CreateChecklistTemplateUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.template.command.CreateChecklistTemplateVersionUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.template.command.EditChecklistTemplateUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.template.query.FindChecklistTemplateByIdUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.template.query.ListChecklistTemplatesUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.template.query.SearchChecklistItemUseCase;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.template.response.ChecklistTemplateResponseDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.mapper.ChecklistTemplateMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChecklistTemplateControllerTest {

    private final SearchChecklistItemUseCase searchChecklistItemUseCase = mock(SearchChecklistItemUseCase.class);
    private final CreateChecklistTemplateVersionUseCase createVersionUseCase = mock(CreateChecklistTemplateVersionUseCase.class);
    private final CreateChecklistTemplateUseCase createUseCase     = mock(CreateChecklistTemplateUseCase.class);
    private final ActivateChecklistTemplateUseCase activateUseCase = mock(ActivateChecklistTemplateUseCase.class);
    private final FindChecklistTemplateByIdUseCase findByIdUseCase = mock(FindChecklistTemplateByIdUseCase.class);
    private final ListChecklistTemplatesUseCase listUseCase        = mock(ListChecklistTemplatesUseCase.class);
    private final EditChecklistTemplateUseCase editUseCase         = mock(EditChecklistTemplateUseCase.class);
    private final ChecklistTemplateMapper mapper                   = mock(ChecklistTemplateMapper.class);
    private final ChecklistTemplateController controller           = new ChecklistTemplateController(
            searchChecklistItemUseCase, createUseCase, activateUseCase, findByIdUseCase, listUseCase, editUseCase, createVersionUseCase, mapper);

    @Test
    @DisplayName("deve retornar ok ao ativar template")
    void deveRetornarOkAoAtivarTemplate() {
        UUID templateId = UUID.randomUUID();
        ChecklistTemplate template = mock(ChecklistTemplate.class);
        ChecklistTemplateResponseDTO response = mock(ChecklistTemplateResponseDTO.class);

        when(activateUseCase.execute(templateId)).thenReturn(template);
        when(mapper.toResponse(template)).thenReturn(response);

        ResponseEntity<ChecklistTemplateResponseDTO> result = controller.activateTemplate(templateId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(activateUseCase).execute(templateId);
        verify(mapper).toResponse(template);
    }
}