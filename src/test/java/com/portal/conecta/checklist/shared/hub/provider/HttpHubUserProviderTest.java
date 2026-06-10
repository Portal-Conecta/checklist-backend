package com.portal.conecta.checklist.shared.hub.provider;

import com.portal.conecta.checklist.shared.hub.client.user.HubUserClient;
import com.portal.conecta.checklist.shared.hub.client.user.HubUserResponse;
import com.portal.conecta.checklist.shared.hub.provider.user.HttpHubUserProvider;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HttpHubUserProviderTest {

    private final HubUserClient hubUserClient = mock(HubUserClient.class);
    private final HttpHubUserProvider provider = new HttpHubUserProvider(hubUserClient);

    @Test
    void shouldFindActiveUserById() {
        UUID userId = UUID.randomUUID();
        when(hubUserClient.findById(userId)).thenReturn(new HubUserResponse(
                userId,
                "Usuario Teste",
                "usuario@estudante.sesisenai.org.br",
                "TEACHER",
                true,
                Instant.now()
        ));

        var reference = provider.findById(userId);

        assertTrue(reference.isPresent());
        assertEquals(userId, reference.orElseThrow().getUserId());
        verify(hubUserClient).findById(userId);
    }
}
