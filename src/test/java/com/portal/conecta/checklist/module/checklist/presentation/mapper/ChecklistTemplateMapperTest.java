package com.portal.conecta.checklist.module.checklist.presentation.mapper;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.domain.model.Status;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.module.checklist.presentation.dto.UserToken;
import com.portal.conecta.checklist.module.checklist.presentation.dto.checklistTemplate.ChecklistTemplateCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.checklistTemplate.ChecklistTemplateResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistTemplate.ChecklistTemplateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ChecklistTemplateMapperTest {

    private ChecklistTemplateMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new ChecklistTemplateMapper() {
            @Override
            public ChecklistTemplateResponseDTO toResponse(ChecklistTemplate entity, RoomReference roomReference) {
                if (entity == null) return null;

                UserToken mockToken = mock(UserToken.class);
                return new ChecklistTemplateResponseDTO(
                        entity.getId(),
                        roomReference,
                        entity.getTitle(),
                        entity.getDescription(),
                        entity.getVersion(),
                        entity.getStatus(),
                        entity.isActive(),
                        mockToken
                );
            }

            @Override
            public ChecklistTemplate toEntity(ChecklistTemplateCreateDTO req) {
                if (req == null) return null;
                return ChecklistTemplate.builder()
                        .roomId(req.roomId())
                        .title(req.title())
                        .status(Status.DRAFT)
                        .build();
            }
        };
    }

    @Test
    @DisplayName("Deve converter CreateDTO para Entidade de Template")
    void deveMapearTemplateParaEntidade() {
        UUID roomId = UUID.randomUUID();
        ChecklistTemplateCreateDTO request = new ChecklistTemplateCreateDTO(
                roomId, "Título Teste", "Descrição Teste", mock(UserToken.class)
        );

        ChecklistTemplate resultado = mapper.toEntity(request);

        assertNotNull(resultado);
        assertEquals(roomId, resultado.getRoomId());
        assertEquals("Título Teste", resultado.getTitle());
        assertEquals(Status.DRAFT, resultado.getStatus());
    }

    @Test
    @DisplayName("Deve converter Entidade e RoomReference para TemplateResponseDTO")
    void deveMapearTemplateParaResponse() {
        ChecklistTemplate entity = ChecklistTemplate.builder()
                .id(UUID.randomUUID())
                .title("Template Salvo")
                .description("Descrição do Template")
                .version(1)
                .status(Status.SUBMITTED)
                .active(true)
                .build();

        RoomReference room = mock(RoomReference.class);

        ChecklistTemplateResponseDTO response = mapper.toResponse(entity, room);

        assertNotNull(response);
        assertEquals(entity.getId(), response.id());
        assertEquals(room, response.roomReference());
        assertEquals(Status.SUBMITTED, response.status());
        assertTrue(response.active());
        assertNotNull(response.schemaJson());
    }
}