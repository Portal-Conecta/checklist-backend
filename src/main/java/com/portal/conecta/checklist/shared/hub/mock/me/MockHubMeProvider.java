package com.portal.conecta.checklist.shared.hub.mock.me;

import com.portal.conecta.checklist.shared.hub.properties.HubMockProperties;
import com.portal.conecta.checklist.shared.hub.provider.me.HubMeProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Valida localmente o usuario autenticado durante testes com o profile mock.
 */
@Component
@Profile({"mock", "test"})
public class MockHubMeProvider implements HubMeProvider {

    private final Set<UUID> ids;

    public MockHubMeProvider(HubMockProperties properties) {
        this.ids = properties.userIds().stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean existsAuthenticatedUser(UUID tokenUserId) {
        return ids.contains(tokenUserId);
    }
}
