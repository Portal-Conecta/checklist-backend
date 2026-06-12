package com.portal.conecta.checklist.shared.hub.mock;

import com.portal.conecta.checklist.shared.hub.mock.me.MockHubMeProvider;
import com.portal.conecta.checklist.shared.hub.properties.HubMockProperties;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockHubMeProviderTest {

    private static final UUID EXISTING_ID =
            UUID.fromString("33333333-3333-3333-3333-333333333331");

    @Test
    void shouldValidateKnownUserId() {
        HubMockProperties properties = new HubMockProperties(
                List.of(),
                List.of(EXISTING_ID.toString()),
                List.of()
        );

        MockHubMeProvider provider = new MockHubMeProvider(properties);

        assertTrue(provider.existsAuthenticatedUser(EXISTING_ID));
        assertFalse(provider.existsAuthenticatedUser(UUID.randomUUID()));
    }
}
