package com.portal.conecta.checklist.shared.integration.hub.mock.me;

import com.portal.conecta.checklist.shared.integration.hub.config.HubMockProperties;
import com.portal.conecta.checklist.shared.integration.hub.adapter.me.HubMeProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Provider mockado do contexto autenticado do Hub.
 *
 * <p>No ambiente mock, o usuario autenticado e validado pelo {@code sub} do token
 * contra a lista local de usuarios conhecidos.</p>
 */
@Component
@Profile({"mock", "test"})
public class MockHubMeProvider implements HubMeProvider {

    private final Set<UUID> ids;

    public MockHubMeProvider(HubMockProperties props) {
        this.ids = props.userIds().stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean existsAuthenticatedUser(UUID tokenUserId) {
        return ids.contains(tokenUserId);
    }
}
