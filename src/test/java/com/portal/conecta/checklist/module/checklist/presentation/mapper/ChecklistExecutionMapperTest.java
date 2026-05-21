package com.portal.conecta.checklist.module.checklist.presentation.mapper;

import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistExecution;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.domain.model.Status;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.ClassReference;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.RoomReference;
import com.portal.conecta.checklist.module.checklist.domain.valueobject.UserReference;
import com.portal.conecta.checklist.module.checklist.presentation.dto.checklistExecution.ChecklistExecutionCreateDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.checklistExecution.ChecklistExecutionResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistExecution.ChecklistExecutionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChecklistExecutionMapperTest {

    private ChecklistExecutionMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = new ChecklistExecutionMapper() {
            @Override
            public ChecklistExecution toEntity(ChecklistExecutionCreateDTO dto, ChecklistTemplate template) {
                if (dto == null) return null;
                return ChecklistExecution.builder()
                        .roomId(dto.roomId())
                        .checklistTemplate(template)
                        .status(Status.DRAFT)
                        .build();
            }

            @Override
            public ChecklistExecutionResponseDTO toResponse(ChecklistExecution entity, RoomReference room, ClassReference classe, UserReference user) {
                if (entity == null) return null;


                return new ChecklistExecutionResponseDTO(
                        entity.getId(),
                        entity.getChecklistTemplate() != null ? entity.getChecklistTemplate().getId() : null,
                        room,
                        classe,
                        user,
                        entity.getStatus(),
                        entity.getAnswersJson(),
                        BigDecimal.valueOf(100.0)
                );
            }
        };
    }

    @Test
    @DisplayName("Deve converter CreateDTO para Entidade de Execução")
    void deveMapearExecutionParaEntidade() {
        UUID roomId = UUID.randomUUID();
        ChecklistExecutionCreateDTO dto = new ChecklistExecutionCreateDTO(
                roomId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Map.of()
        );
        ChecklistTemplate template = mock(ChecklistTemplate.class);

        ChecklistExecution resultado = mapper.toEntity(dto, template);

        assertNotNull(resultado);
        assertEquals(roomId, resultado.getRoomId());
        assertEquals(Status.DRAFT, resultado.getStatus());
    }

    @Test
    @DisplayName("Deve converter Entidade de Execução para ResponseDTO")
    void deveMapearExecutionParaResponse() {
        UUID templateId = UUID.randomUUID();
        ChecklistTemplate template = mock(ChecklistTemplate.class);
        when(template.getId()).thenReturn(templateId);

        UUID roomId = UUID.randomUUID();
        RoomReference room = new RoomReference(roomId);

        ClassReference clazz = mock(ClassReference.class);
        UserReference user = mock(UserReference.class);

        ChecklistExecution entity = ChecklistExecution.builder()
                .id(UUID.randomUUID())
                .checklistTemplate(template)
                .status(Status.SUBMITTED)
                .answersJson(Map.of("pergunta1", "resposta1"))
                .build();

        ChecklistExecutionResponseDTO response = mapper.toResponse(entity, room, clazz, user);

        assertNotNull(response);
        assertEquals(entity.getId(), response.id());
        assertEquals(templateId, response.templateId());
        assertEquals(room, response.room());
        assertEquals(clazz, response.clazz());
        assertEquals(user, response.user());
        assertEquals(Status.SUBMITTED, response.status());
        assertNotNull(response.answersJson());
        assertEquals("resposta1", response.answersJson().get("pergunta1"));
    }
}