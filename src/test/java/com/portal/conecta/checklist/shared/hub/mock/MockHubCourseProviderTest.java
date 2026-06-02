package com.portal.conecta.checklist.shared.hub.mock;

import com.portal.conecta.checklist.shared.hub.mock.course.MockHubCourseProvider;
import com.portal.conecta.checklist.shared.hub.properties.HubMockProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockHubCourseProviderTest {

    private static final UUID ID_EXISTENTE = UUID.fromString("55555555-5555-5555-5555-555555555551");
    private static final UUID ID_INEXISTENTE = UUID.fromString("99999999-9999-9999-9999-999999999999");

    private MockHubCourseProvider provider;

    @BeforeEach
    void setUp() {
        HubMockProperties props = new HubMockProperties(
                List.of(),
                List.of(),
                List.of(),
                List.of("55555555-5555-5555-5555-555555555551")
        );
        provider = new MockHubCourseProvider(props);
    }

    @Test
    @DisplayName("deve retornar true quando o curso existe")
    void deveRetornarTrueQuandoCursoExiste() {
        assertTrue(provider.existsById(ID_EXISTENTE));
    }

    @Test
    @DisplayName("deve retornar referencia quando o curso existe")
    void deveRetornarReferenciaQuandoCursoExiste() {
        assertTrue(provider.findById(ID_EXISTENTE).isPresent());
    }

    @Test
    @DisplayName("deve retornar false quando o curso nao existe")
    void deveRetornarFalseQuandoCursoNaoExiste() {
        assertFalse(provider.existsById(ID_INEXISTENTE));
    }

    @Test
    @DisplayName("deve retornar false quando lista de cursos esta vazia")
    void deveRetornarFalseQuandoListaVazia() {
        HubMockProperties props = new HubMockProperties(List.of(), List.of(), List.of(), List.of());
        MockHubCourseProvider providerVazio = new MockHubCourseProvider(props);

        assertFalse(providerVazio.existsById(ID_EXISTENTE));
    }
}
