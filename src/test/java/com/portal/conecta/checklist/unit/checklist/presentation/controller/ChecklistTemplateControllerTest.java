package com.portal.conecta.checklist.unit.checklist.presentation.controller;

import com.portal.conecta.checklist.module.checklist.application.usecase.template.command.activate.ActivateChecklistTemplateUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.command.create.CreateChecklistTemplateUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.command.create.CreateChecklistTemplateVersionUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.command.edit.UpdateChecklistTemplateUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.query.find.FindChecklistTemplateByIdUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.query.list.ListChecklistTemplatesUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.query.search.SearchChecklistItemUseCase;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.query.search.SearchItemsByCategoryUseCase;
import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.module.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.module.checklist.application.usecase.template.query.search.ChecklistItemByCategoryResult;
import com.portal.conecta.checklist.module.checklist.presentation.dto.template.response.ChecklistItemByCategorySearchResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.controller.ChecklistTemplateController;
import com.portal.conecta.checklist.module.checklist.presentation.dto.template.response.ChecklistTemplateResponseDTO;
import com.portal.conecta.checklist.module.checklist.presentation.mapper.ChecklistTemplateMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChecklistTemplateControllerTest {

    private final SearchChecklistItemUseCase searchChecklistItemUseCase = mock(SearchChecklistItemUseCase.class);
    private final SearchItemsByCategoryUseCase searchItemsByCategoryUseCase = mock(SearchItemsByCategoryUseCase.class);
    private final CreateChecklistTemplateVersionUseCase createVersionUseCase = mock(CreateChecklistTemplateVersionUseCase.class);
    private final CreateChecklistTemplateUseCase createUseCase     = mock(CreateChecklistTemplateUseCase.class);
    private final ActivateChecklistTemplateUseCase activateUseCase = mock(ActivateChecklistTemplateUseCase.class);
    private final FindChecklistTemplateByIdUseCase findByIdUseCase = mock(FindChecklistTemplateByIdUseCase.class);
    private final ListChecklistTemplatesUseCase listUseCase        = mock(ListChecklistTemplatesUseCase.class);
    private final UpdateChecklistTemplateUseCase editUseCase         = mock(UpdateChecklistTemplateUseCase.class);
    private final ChecklistTemplateMapper mapper                   = mock(ChecklistTemplateMapper.class);
    private final ChecklistTemplateController controller           = new ChecklistTemplateController(
        searchChecklistItemUseCase, searchItemsByCategoryUseCase, createUseCase, activateUseCase, findByIdUseCase, listUseCase, editUseCase, createVersionUseCase, mapper);

    @Test
    @DisplayName("deve retornar ok ao ativar template")
    void deveRetornarOkAoAtivarTemplate() {
        UUID templateId = UUID.randomUUID();
        ChecklistTemplate template = mock(ChecklistTemplate.class);
        ChecklistTemplateResponseDTO response = mock(ChecklistTemplateResponseDTO.class);

        when(activateUseCase.execute(templateId)).thenReturn(template);
        when(mapper.toResponseWithEnrichment(template)).thenReturn(response);

        ResponseEntity<ChecklistTemplateResponseDTO> result = controller.activateTemplate(templateId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(activateUseCase).execute(templateId);
        verify(mapper).toResponseWithEnrichment(template);
    }

    @Test
    @DisplayName("deve buscar itens por categoria com sucesso")
    void deveBuscarItensPorCategoriaComSucesso() {
        String category = "Limpeza";
        ChecklistItemByCategoryResult resultItem = new ChecklistItemByCategoryResult(
            UUID.randomUUID(), "Template", "sec-1", "Secao", "item-1", "Item", "Descricao", true, 1, category);

        when(searchItemsByCategoryUseCase.execute(category)).thenReturn(List.of(resultItem));

        ResponseEntity<List<ChecklistItemByCategorySearchResponseDTO>> result = controller.searchItemsByCategory(category);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals("item-1", result.getBody().get(0).key());
        assertEquals(category, result.getBody().get(0).category());
        verify(searchItemsByCategoryUseCase).execute(category);
    }

    @Test
    @DisplayName("deve expor busca por categoria pela rota HTTP")
    void deveExporBuscaPorCategoriaPelaRotaHttp() throws Exception {
        String category = "Limpeza";
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(searchItemsByCategoryUseCase.execute(category)).thenReturn(List.of());

        mockMvc.perform(get("/api/checklist-templates/items/search").param("category", category))
            .andExpect(status().isOk());

        verify(searchItemsByCategoryUseCase).execute(category);
    }

    @Test
    @DisplayName("deve listar templates sem filtros mantendo compatibilidade")
    void deveListarTemplatesSemFiltros() {
        List<ChecklistTemplate> templates = List.of(mock(ChecklistTemplate.class));
        List<ChecklistTemplateResponseDTO> response = List.of(mock(ChecklistTemplateResponseDTO.class));

        when(listUseCase.execute(null, null, null)).thenReturn(templates);
        when(mapper.toResponseListWithEnrichment(templates)).thenReturn(response);

        ResponseEntity<List<ChecklistTemplateResponseDTO>> result = controller.listTemplates(null, null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(listUseCase).execute(null, null, null);
    }

    @Test
    @DisplayName("deve listar templates filtrando por roomId e status")
    void deveListarTemplatesFiltrandoPorRoomIdEStatus() {
        UUID roomId = UUID.randomUUID();
        ChecklistTemplateStatus status = ChecklistTemplateStatus.ACTIVE;
        List<ChecklistTemplate> templates = List.of(mock(ChecklistTemplate.class));
        List<ChecklistTemplateResponseDTO> response = List.of(mock(ChecklistTemplateResponseDTO.class));

        when(listUseCase.execute(roomId, status, null)).thenReturn(templates);
        when(mapper.toResponseListWithEnrichment(templates)).thenReturn(response);

        ResponseEntity<List<ChecklistTemplateResponseDTO>> result = controller.listTemplates(roomId, status, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(response, result.getBody());
        verify(listUseCase).execute(roomId, status, null);
    }

    @Test
    @DisplayName("deve aceitar apenas roomId via query param")
    void deveListarTemplatesFiltrandoApenasPorRoomId() throws Exception {
        UUID roomId = UUID.randomUUID();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(listUseCase.execute(roomId, null, null)).thenReturn(List.of());
        when(mapper.toResponseListWithEnrichment(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/checklist-templates").param("roomId", roomId.toString()))
            .andExpect(status().isOk());

        verify(listUseCase).execute(roomId, null, null);
    }

    @Test
    @DisplayName("deve aceitar apenas status via query param")
    void deveListarTemplatesFiltrandoApenasPorStatus() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(listUseCase.execute(null, ChecklistTemplateStatus.ACTIVE, null)).thenReturn(List.of());
        when(mapper.toResponseListWithEnrichment(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/checklist-templates").param("status", "ACTIVE"))
            .andExpect(status().isOk());

        verify(listUseCase).execute(null, ChecklistTemplateStatus.ACTIVE, null);
    }

    @Test
    @DisplayName("deve aceitar roomId e status juntos via query param")
    void deveListarTemplatesFiltrandoPorRoomIdEStatusViaQueryParam() throws Exception {
        UUID roomId = UUID.randomUUID();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(listUseCase.execute(roomId, ChecklistTemplateStatus.ACTIVE, null)).thenReturn(List.of());
        when(mapper.toResponseListWithEnrichment(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/checklist-templates")
                .param("roomId", roomId.toString())
                .param("status", "ACTIVE"))
            .andExpect(status().isOk());

        verify(listUseCase).execute(roomId, ChecklistTemplateStatus.ACTIVE, null);
    }
}
