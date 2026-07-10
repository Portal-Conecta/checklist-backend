package com.portal.conecta.checklist.modules.checklist.application.usecase.template.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.application.usecase.template.query.search.SearchItemsByCategoryUseCase;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistTemplateRepository;
<<<<<<< HEAD
import com.portal.conecta.checklist.modules.checklist.presentation.dto.template.response.ChecklistItemByCategorySearchResponseDTO;
=======
import com.portal.conecta.checklist.modules.checklist.presentation.dto.template.response.ChecklistItemSearchResponseDTO;
>>>>>>> 02e7cb3b7e26872d52e06b8aae70a43777a962eb
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.context.TypeUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SearchItemsByCategoryUseCaseTest {

    private final ChecklistTemplateRepository templateRepository = mock(ChecklistTemplateRepository.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SearchItemsByCategoryUseCase useCase = new SearchItemsByCategoryUseCase(
            templateRepository,
            contextProvider,
            objectMapper
    );

    @Test
    void shouldRejectApprenticeAccess() {
        when(contextProvider.getRequestContext()).thenReturn(apprentice());

        assertThrows(AccessDeniedException.class, () -> useCase.execute("Limpeza"));

        verify(templateRepository, never()).findAllByActiveTrueAndStatus(any());
    }

    @Test
    void shouldReturnEmptyListIfCategoryNameIsBlank() {
        when(contextProvider.getRequestContext()).thenReturn(senai());

<<<<<<< HEAD
        List<ChecklistItemByCategorySearchResponseDTO> resultNull = useCase.execute(null);
        List<ChecklistItemByCategorySearchResponseDTO> resultBlank = useCase.execute("   ");
=======
        List<ChecklistItemSearchResponseDTO> resultNull = useCase.execute(null);
        List<ChecklistItemSearchResponseDTO> resultBlank = useCase.execute("   ");
>>>>>>> 02e7cb3b7e26872d52e06b8aae70a43777a962eb

        assertEquals(0, resultNull.size());
        assertEquals(0, resultBlank.size());
        verify(templateRepository, never()).findAllByActiveTrueAndStatus(any());
    }

    @Test
    void shouldReturnFilteredItemsByCategoryCaseInsensitively() {
        UUID templateId = UUID.randomUUID();
        Map<String, Object> schemaMap = Map.of(
                "sections", List.of(
                        Map.of(
                                "key", "sec-1",
                                "title", "Seção de Teste",
                                "order", 1,
                                "items", List.of(
                                        Map.of(
                                                "key", "item-1",
                                                "title", "Quadro Limpo",
                                                "description", "Verificar limpeza do quadro",
                                                "required", true,
                                                "order", 1,
                                                "category", "Limpeza"
                                        ),
                                        Map.of(
                                                "key", "item-2",
                                                "title", "Porta Trancada",
                                                "description", "Verificar fechadura",
                                                "required", false,
                                                "order", 2,
                                                "category", "Segurança"
                                        )
                                )
                        )
                )
        );

        ChecklistTemplate activeTemplate = ChecklistTemplate.builder()
                .id(templateId)
                .title("Checklist de Sala")
                .active(true)
                .status(ChecklistTemplateStatus.ACTIVE)
                .schemaJson(schemaMap)
                .build();

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE))
                .thenReturn(List.of(activeTemplate));

        // Test search
<<<<<<< HEAD
        List<ChecklistItemByCategorySearchResponseDTO> results = useCase.execute("  limpeza  ");

        assertEquals(1, results.size());
        ChecklistItemByCategorySearchResponseDTO item = results.getFirst();
=======
        List<ChecklistItemSearchResponseDTO> results = useCase.execute("  limpeza  ");

        assertEquals(1, results.size());
        ChecklistItemSearchResponseDTO item = results.getFirst();
>>>>>>> 02e7cb3b7e26872d52e06b8aae70a43777a962eb
        assertEquals(templateId, item.templateId());
        assertEquals("Checklist de Sala", item.templateTitle());
        assertEquals("sec-1", item.sectionKey());
        assertEquals("Seção de Teste", item.sectionTitle());
        assertEquals("item-1", item.key());
        assertEquals("Quadro Limpo", item.title());
        assertEquals("Verificar limpeza do quadro", item.description());
        assertEquals(true, item.required());
        assertEquals(1, item.order());
        assertEquals("Limpeza", item.category());
    }

<<<<<<< HEAD
    @Test
    void shouldSkipTemplateWithInvalidSchema() {
        UUID validTemplateId = UUID.randomUUID();
        UUID invalidTemplateId = UUID.randomUUID();

        Map<String, Object> validSchema = Map.of(
                "sections", List.of(
                        Map.of(
                                "key", "sec-1",
                                "title", "Seção",
                                "order", 1,
                                "items", List.of(
                                        Map.of(
                                                "key", "item-1",
                                                "title", "Item válido",
                                                "required", true,
                                                "order", 1,
                                                "category", "Limpeza"
                                        )
                                )
                        )
                )
        );

        ChecklistTemplate validTemplate = ChecklistTemplate.builder()
                .id(validTemplateId)
                .title("Template válido")
                .active(true)
                .status(ChecklistTemplateStatus.ACTIVE)
                .schemaJson(validSchema)
                .build();

        ChecklistTemplate invalidTemplate = ChecklistTemplate.builder()
                .id(invalidTemplateId)
                .title("Template inválido")
                .active(true)
                .status(ChecklistTemplateStatus.ACTIVE)
                .schemaJson(Map.of("sections", "schema-invalido"))
                .build();

        when(contextProvider.getRequestContext()).thenReturn(senai());
        when(templateRepository.findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE))
                .thenReturn(List.of(invalidTemplate, validTemplate));

        List<ChecklistItemByCategorySearchResponseDTO> results = useCase.execute("Limpeza");

        assertEquals(1, results.size());
        assertEquals(validTemplateId, results.getFirst().templateId());
    }

=======
>>>>>>> 02e7cb3b7e26872d52e06b8aae70a43777a962eb
    private RequestContext apprentice() {
        return new RequestContext(UUID.randomUUID(), TypeUser.STUDENT);
    }

    private RequestContext senai() {
        return new RequestContext(UUID.randomUUID(), TypeUser.SENAI);
    }
}
