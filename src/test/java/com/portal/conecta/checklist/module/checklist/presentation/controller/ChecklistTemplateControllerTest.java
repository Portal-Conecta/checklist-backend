package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistTemplateResponseDTO;
import com.portal.conecta.checklist.module.checklist.application.facade.ChecklistTemplateFacade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChecklistTemplateControllerTest {

    private final ChecklistTemplateFacade facade = mock(ChecklistTemplateFacade.class);
    private final ChecklistTemplateController controller = new ChecklistTemplateController(facade);

    @Test
    @DisplayName("deve retornar ok ao ativar template")
    void deveRetornarOkAoAtivarTemplate() {
        UUID templateId = UUID.randomUUID();
        ChecklistTemplateResponseDTO response = mock(ChecklistTemplateResponseDTO.class);

        when(facade.activateTemplate(templateId)).thenReturn(response);

        ResponseEntity<ChecklistTemplateResponseDTO> result = controller.activateTemplate(templateId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(facade).activateTemplate(templateId);
    }
    @Test
    @DisplayName("deve retornar lista paginada de templates")
    void deveRetornarListaPaginadaDeTemplates() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<ChecklistTemplateResponseDTO> page =
                new PageImpl<>(java.util.List.of());

        when(facade.listTemplates(pageable))
                .thenReturn(page);

        ResponseEntity<Page<ChecklistTemplateResponseDTO>> result =
                controller.listTemplates(pageable);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(page, result.getBody());

        verify(facade).listTemplates(pageable);
    }
}
