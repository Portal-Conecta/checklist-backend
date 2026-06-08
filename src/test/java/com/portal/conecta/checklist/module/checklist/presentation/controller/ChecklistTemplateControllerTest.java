package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.usecase.template.ActivateChecklistTemplateUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.CreateChecklistTemplateUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.FindChecklistTemplateByIdUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.ListChecklistTemplatesUseCase;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistTemplateResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistTemplateMapper;
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

    private final CreateChecklistTemplateUseCase createUseCase    = mock(CreateChecklistTemplateUseCase.class);
    private final ActivateChecklistTemplateUseCase activateUseCase = mock(ActivateChecklistTemplateUseCase.class);
    private final FindChecklistTemplateByIdUseCase findByIdUseCase = mock(FindChecklistTemplateByIdUseCase.class);
    private final ListChecklistTemplatesUseCase listUseCase        = mock(ListChecklistTemplatesUseCase.class);
    private final ChecklistTemplateMapper mapper                   = mock(ChecklistTemplateMapper.class);
    private final ChecklistTemplateController controller           = new ChecklistTemplateController(
            createUseCase, activateUseCase, findByIdUseCase, listUseCase, mapper);

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
