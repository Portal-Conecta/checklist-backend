package com.portal.conecta.checklist.shared.integration.hub.mock;

import com.portal.conecta.checklist.shared.integration.hub.mock.me.MockHubMeProvider;
import com.portal.conecta.checklist.shared.integration.hub.config.HubMockProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockHubMeProviderTest {

    private static final UUID ID_EXISTENTE = UUID.fromString("33333333-3333-3333-3333-333333333331");
    private static final UUID ID_INEXISTENTE = UUID.fromString("99999999-9999-9999-9999-999999999999");

    private MockHubMeProvider provider;

    @BeforeEach
    void setUp() {
        HubMockProperties props = new HubMockProperties(
                List.of(),
                List.of(
                        "33333333-3333-3333-3333-333333333331",
                        "33333333-3333-3333-3333-333333333332"
                ),
                List.of()
        );
        provider = new MockHubMeProvider(props);
    }

    @Test
    @DisplayName("deve retornar true quando o usuario do token existe")
    void deveRetornarTrueQuandoUsuarioExiste() {
        assertTrue(provider.existsAuthenticatedUser(ID_EXISTENTE));
    }

    @Test
    @DisplayName("deve retornar false quando o usuario do token nao existe")
    void deveRetornarFalseQuandoUsuarioNaoExiste() {
        assertFalse(provider.existsAuthenticatedUser(ID_INEXISTENTE));
    }

    @Test
    @DisplayName("deve retornar false quando lista de usuarios esta vazia")
    void deveRetornarFalseQuandoListaVazia() {
        HubMockProperties props = new HubMockProperties(List.of(), List.of(), List.of());
        MockHubMeProvider providerVazio = new MockHubMeProvider(props);

        assertFalse(providerVazio.existsAuthenticatedUser(ID_EXISTENTE));
    }
}
