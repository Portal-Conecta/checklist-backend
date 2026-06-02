package com.portal.conecta.checklist.module.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.facade.ChecklistExecutionFacade;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.response.ChecklistExecutionResponseDTO;
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

class ChecklistExecutionControllerTest {

    private final ChecklistExecutionFacade facade    = mock(ChecklistExecutionFacade.class);
    private final ChecklistExecutionController controller = new ChecklistExecutionController(facade);

    @Test
    @DisplayName("deve retornar created ao criar draft")
    void deveRetornarCreatedAoCriarDraft() {
        ChecklistExecutionDraftCreateDTO request = new ChecklistExecutionDraftCreateDTO(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), ChecklistType.ARRIVAL);
        ChecklistExecutionResponseDTO response = mock(ChecklistExecutionResponseDTO.class);

        when(facade.createDTO(request)).thenReturn(response);

        ResponseEntity<ChecklistExecutionResponseDTO> result = controller.createDraft(request);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(facade).createDTO(request);
    }
}
