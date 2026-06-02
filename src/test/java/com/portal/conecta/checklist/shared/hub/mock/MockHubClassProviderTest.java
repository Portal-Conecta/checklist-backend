package com.portal.conecta.checklist.shared.hub.mock;

import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.shared.hub.mock.classes.MockHubClassProvider;
import com.portal.conecta.checklist.shared.hub.properties.HubMockProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockHubClassProviderTest {

    private static final UUID ID_EXISTENTE = UUID.fromString("22222222-2222-2222-2222-222222222221");
    private static final UUID ID_INEXISTENTE = UUID.fromString("99999999-9999-9999-9999-999999999999");

    private MockHubClassProvider provider;

    @BeforeEach
    void setUp() {
        HubMockProperties props = new HubMockProperties(
                List.of(
                        "22222222-2222-2222-2222-222222222221",
                        "22222222-2222-2222-2222-222222222222"
                ),
                List.of(),
                List.of(),
                List.of(),
                Map.of(
                        "22222222-2222-2222-2222-222222222221", "FULL_AM_PM",
                        "22222222-2222-2222-2222-222222222222", "FULL_PM_NT"
                )
        );
        provider = new MockHubClassProvider(props);
    }

    @Test
    @DisplayName("deve retornar true quando a turma existe")
    void deveRetornarTrueQuandoTurmaExiste() {
        assertTrue(provider.existsById(ID_EXISTENTE));
    }

    @Test
    @DisplayName("deve retornar false quando a turma nao existe")
    void deveRetornarFalseQuandoTurmaNaoExiste() {
        assertFalse(provider.existsById(ID_INEXISTENTE));
    }

    @Test
    @DisplayName("deve retornar shift quando a turma existe")
    void deveRetornarShiftQuandoTurmaExiste() {
        var reference = provider.findById(ID_EXISTENTE).orElseThrow();

        assertEquals(Shift.FULL_AM_PM, reference.getShift());
        assertEquals(Shift.FULL_AM_PM, provider.findShiftByClassId(ID_EXISTENTE).orElseThrow());
    }

    @Test
    @DisplayName("deve retornar false quando lista de turmas esta vazia")
    void deveRetornarFalseQuandoListaVazia() {
        HubMockProperties props = new HubMockProperties(List.of(), List.of(), List.of());
        MockHubClassProvider providerVazio = new MockHubClassProvider(props);

        assertFalse(providerVazio.existsById(ID_EXISTENTE));
    }
}
