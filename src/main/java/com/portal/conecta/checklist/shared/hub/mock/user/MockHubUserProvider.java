package com.portal.conecta.checklist.shared.hub.mock.user;

import com.portal.conecta.checklist.shared.hub.properties.HubMockProperties;
import com.portal.conecta.checklist.shared.hub.provider.user.HubUserProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Profile({"mock", "test"})
public class MockHubUserProvider implements HubUserProvider {

    private final Set<UUID> ids;

    public MockHubUserProvider(HubMockProperties props){
        this.ids = props.userIds().stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean existsById(UUID userId) {
        return ids.contains(userId);
    }
}
