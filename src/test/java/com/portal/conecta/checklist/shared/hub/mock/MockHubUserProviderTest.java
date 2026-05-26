package com.portal.conecta.checklist.shared.hub.mock;

import com.portal.conecta.checklist.shared.hub.mock.user.MockHubUserProvider;
import com.portal.conecta.checklist.shared.hub.properties.HubMockProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MockHubUserProviderTest {

    private static final UUID ID_EXISTENTE   = UUID.fromString("33333333-3333-3333-3333-333333333331");
    private static final UUID ID_INEXISTENTE = UUID.fromString("99999999-9999-9999-9999-999999999999");

    private MockHubUserProvider provider;

    @BeforeEach
    void setUp() {
        HubMockProperties props = new HubMockProperties(
                List.of(),
                List.of("33333333-3333-3333-3333-333333333331",
                        "33333333-3333-3333-3333-333333333332"),
                List.of()
        );
        provider = new MockHubUserProvider(props);
    }

    @Test
    @DisplayName("deve retornar true quando o usuário existe")
    void deveRetornarTrueQuandoUsuarioExiste() {
        assertTrue(provider.existsById(ID_EXISTENTE));
    }

    @Test
    @DisplayName("deve retornar false quando o usuário não existe")
    void deveRetornarFalseQuandoUsuarioNaoExiste() {
        assertFalse(provider.existsById(ID_INEXISTENTE));
    }

    @Test
    @DisplayName("deve retornar false quando lista de usuários está vazia")
    void deveRetornarFalseQuandoListaVazia() {
        HubMockProperties props = new HubMockProperties(List.of(), List.of(), List.of());
        MockHubUserProvider providerVazio = new MockHubUserProvider(props);

        assertFalse(providerVazio.existsById(ID_EXISTENTE));
    }
}