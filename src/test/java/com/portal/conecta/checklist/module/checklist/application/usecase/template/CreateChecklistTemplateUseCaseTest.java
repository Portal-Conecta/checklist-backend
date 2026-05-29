package com.portal.conecta.checklist.module.checklist.application.usecase.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.module.checklist.application.mapper.ChecklistTemplateCommandMapper;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.domain.validation.ChecklistTemplateLimits;
import com.portal.conecta.checklist.module.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistTemplateCreateRequest;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistItemDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSchemaDTO;
import com.portal.conecta.checklist.module.checklist.presentation.dto.schema.ChecklistSectionDTO;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import com.portal.conecta.checklist.shared.hub.provider.room.HubRoomProvider;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateChecklistTemplateUseCaseTest {

    private final ChecklistTemplateRepository templateRepository = mock(ChecklistTemplateRepository.class);
    private final HubRoomProvider hubRoomProvider = mock(HubRoomProvider.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final ChecklistTemplateCommandMapper mapper = new ChecklistTemplateCommandMapper(new ObjectMapper());
    private final CreateChecklistTemplateUseCase useCase = new CreateChecklistTemplateUseCase(
            templateRepository,
            hubRoomProvider,
            contextProvider,
            mapper
    );

    @Test
    void shouldCreateTemplateWhenManagerAndRoomExists() {
        UUID roomId = UUID.randomUUID();
        ChecklistTemplateCreateRequest request = request(roomId);

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(hubRoomProvider.existsById(roomId)).thenReturn(true);
        when(templateRepository.save(any(ChecklistTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChecklistTemplate result = useCase.execute(request);

        assertThat(result.getRoomId()).isEqualTo(roomId);
        assertThat(result.getTitle()).isEqualTo("Checklist padrao");
        verify(templateRepository).save(any(ChecklistTemplate.class));
    }

    @Test
    void shouldRejectWhenUserCannotManageTemplates() {
        ChecklistTemplateCreateRequest request = request(UUID.randomUUID());

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.STUDENT));

        assertThrows(AccessDeniedException.class, () -> useCase.execute(request));

        verify(hubRoomProvider, never()).existsById(any());
        verify(templateRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenRoomDoesNotExistInHub() {
        UUID roomId = UUID.randomUUID();
        ChecklistTemplateCreateRequest request = request(roomId);

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));
        when(hubRoomProvider.existsById(roomId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> useCase.execute(request));

        verify(templateRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenSchemaHasTooManySections() {
        UUID roomId = UUID.randomUUID();
        ChecklistTemplateCreateRequest request = request(
                roomId,
                IntStream.rangeClosed(1, ChecklistTemplateLimits.MAX_SECTIONS + 1)
                        .mapToObj(section -> 1)
                        .toList()
        );

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));

        assertThat(exception.getMessage()).contains("limite de " + ChecklistTemplateLimits.MAX_SECTIONS + " secoes");
        verify(hubRoomProvider, never()).existsById(any());
        verify(templateRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenSectionHasTooManyItems() {
        UUID roomId = UUID.randomUUID();
        ChecklistTemplateCreateRequest request = request(
                roomId,
                List.of(ChecklistTemplateLimits.MAX_ITEMS_PER_SECTION + 1)
        );

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));

        assertThat(exception.getMessage())
                .contains("limite de " + ChecklistTemplateLimits.MAX_ITEMS_PER_SECTION + " itens por secao");
        verify(hubRoomProvider, never()).existsById(any());
        verify(templateRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenSchemaHasTooManyItemsInTotal() {
        UUID roomId = UUID.randomUUID();
        ChecklistTemplateCreateRequest request = request(roomId, itemCountsExceedingTotalLimit());

        when(contextProvider.getRequestContext()).thenReturn(user(TypeUser.SENAI));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));

        assertThat(exception.getMessage())
                .contains("limite total de " + ChecklistTemplateLimits.MAX_TOTAL_ITEMS + " itens");
        verify(hubRoomProvider, never()).existsById(any());
        verify(templateRepository, never()).save(any());
    }

    private ChecklistTemplateCreateRequest request(UUID roomId) {
        return request(roomId, List.of(1));
    }

    private ChecklistTemplateCreateRequest request(UUID roomId, List<Integer> itemsPerSection) {
        return new ChecklistTemplateCreateRequest(
                roomId,
                "Checklist padrao",
                "Descricao",
                new ChecklistSchemaDTO(IntStream.range(0, itemsPerSection.size())
                        .mapToObj(sectionIndex -> section(sectionIndex + 1, itemsPerSection.get(sectionIndex)))
                        .toList())
        );
    }

    private ChecklistSectionDTO section(int sectionIndex, int itemCount) {
        return new ChecklistSectionDTO(
                "secao-" + sectionIndex,
                "Secao " + sectionIndex,
                sectionIndex,
                IntStream.rangeClosed(1, itemCount)
                        .mapToObj(itemIndex -> item(sectionIndex, itemIndex))
                        .toList()
        );
    }

    private ChecklistItemDTO item(int sectionIndex, int itemIndex) {
        return new ChecklistItemDTO(
                "item-" + sectionIndex + "-" + itemIndex,
                "Item " + sectionIndex + "." + itemIndex,
                "Verificar item",
                true,
                itemIndex
        );
    }

    private List<Integer> itemCountsExceedingTotalLimit() {
        List<Integer> itemCounts = new ArrayList<>();
        int remainingItems = ChecklistTemplateLimits.MAX_TOTAL_ITEMS + 1;

        while (remainingItems > 0) {
            int sectionItems = Math.min(ChecklistTemplateLimits.MAX_ITEMS_PER_SECTION, remainingItems);
            itemCounts.add(sectionItems);
            remainingItems -= sectionItems;
        }

        return itemCounts;
    }

    private RequestContext user(TypeUser userType) {
        return new RequestContext(UUID.randomUUID(), userType);
    }
}
