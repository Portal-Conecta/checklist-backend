package com.portal.conecta.checklist.shared.hub.mock;

import com.portal.conecta.checklist.shared.hub.mock.room.MockHubRoomProvider;
import com.portal.conecta.checklist.shared.hub.properties.HubMockProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MockHubRoomProviderTest {

    private static final UUID ID_EXISTENTE   = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ID_INEXISTENTE = UUID.fromString("99999999-9999-9999-9999-999999999999");

    private MockHubRoomProvider provider;

    @BeforeEach
    void setUp() {
        HubMockProperties props = new HubMockProperties(
                List.of(),
                List.of(),
                List.of("11111111-1111-1111-1111-111111111111",
                        "11111111-1111-1111-1111-111111111112")
        );
        provider = new MockHubRoomProvider(props);
    }

    @Test
    @DisplayName("deve retornar true quando a sala existe")
    void deveRetornarTrueQuandoSalaExiste() {
        assertTrue(provider.existsById(ID_EXISTENTE));
    }

    @Test
    @DisplayName("deve retornar false quando a sala não existe")
    void deveRetornarFalseQuandoSalaNaoExiste() {
        assertFalse(provider.existsById(ID_INEXISTENTE));
    }

    @Test
    @DisplayName("deve retornar false quando lista de salas está vazia")
    void deveRetornarFalseQuandoListaVazia() {
        HubMockProperties props = new HubMockProperties(List.of(), List.of(), List.of());
        MockHubRoomProvider providerVazio = new MockHubRoomProvider(props);

        assertFalse(providerVazio.existsById(ID_EXISTENTE));
    }
}