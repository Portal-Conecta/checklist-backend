package com.portal.conecta.checklist.unit.checklist.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portal.conecta.checklist.modules.checklist.domain.enums.ChecklistTemplateStatus;
import com.portal.conecta.checklist.modules.checklist.domain.model.ChecklistTemplate;
import com.portal.conecta.checklist.modules.checklist.domain.schema.ChecklistItem;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistItemQueryRepository;
import com.portal.conecta.checklist.modules.checklist.infrastructure.persistence.ChecklistTemplateRepository;
import com.portal.conecta.checklist.shared.context.AbstractRepositoryTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChecklistItemQueryRepositoryTest extends AbstractRepositoryTest {

    @Mock
    private ChecklistTemplateRepository checklistTemplateRepository;

    @Spy // Usamos Spy no ObjectMapper para ele funcionar de verdade sem precisar de Mock complexo
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ChecklistItemQueryRepository queryRepository;

    private ChecklistTemplate templateFake;

    @BeforeEach
    void setUp() {
        // Monta a estrutura de um JSONB simulado (Map) contendo as seções e os itens internos
        Map<String, Object> item1 = Map.of("key", "it-1", "title", "Verificar Óleo", "description", "Checar nível do motor", "required", true, "order", 1);
        Map<String, Object> item2 = Map.of("key", "it-2", "title", "Calibragem Pneus", "description", "Garantir 32 PSI", "required", false, "order", 2);

        Map<String, Object> section = Map.of("items", List.of(item1, item2));
        Map<String, Object> schemaJson = Map.of("sections", List.of(section));

        // Cria o mock do template de domínio que contém o JSON
        templateFake = mock(ChecklistTemplate.class);
        when(templateFake.getSchemaJson()).thenReturn(schemaJson);
    }

    @Test
    @DisplayName("Deve retornar itens que possuem o termo buscado no título")
    void deveRetornarItensQuandoTermoEstiverNoTitulo() {
        // GIVEN
        when(checklistTemplateRepository.findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE))
                .thenReturn(List.of(templateFake));

        // WHEN
        List<ChecklistItem> resultado = queryRepository.searchByTitleOrDescription("Óleo");

        // THEN
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Verificar Óleo", resultado.get(0).title());
    }

    @Test
    @DisplayName("Deve retornar itens que possuem o termo buscado na descrição (case insensitive)")
    void deveRetornarItensQuandoTermoEstiverNaDescricaoEIgnorarMaiusculas() {
        // GIVEN
        when(checklistTemplateRepository.findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE))
                .thenReturn(List.of(templateFake));

        // WHEN (buscando em minúsculo por parte da descrição "Garantir 32 PSI")
        List<ChecklistItem> resultado = queryRepository.searchByTitleOrDescription("psi");

        // THEN
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Calibragem Pneus", resultado.get(0).title());
    }

    @Test
    @DisplayName("Deve retornar lista vazia se nenhum item corresponder ao termo")
    void deveRetornarVazioQuandoNenhumItemCorresponder() {
        // GIVEN
        when(checklistTemplateRepository.findAllByActiveTrueAndStatus(ChecklistTemplateStatus.ACTIVE))
                .thenReturn(List.of(templateFake));

        // WHEN
        List<ChecklistItem> resultado = queryRepository.searchByTitleOrDescription("Texto Inexistente");

        // THEN
        assertTrue(resultado.isEmpty());
    }
}