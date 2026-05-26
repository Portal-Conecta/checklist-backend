package com.portal.conecta.checklist.shared.hub.mock;

import com.portal.conecta.checklist.shared.hub.properties.HubMockProperties;
import com.portal.conecta.checklist.shared.hub.provider.HubRoomProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
@Component
@Profile("mock")
public class MockHubRoomProvider implements HubRoomProvider {

    private final Set<UUID> ids;

    public MockHubRoomProvider(HubMockProperties props){
        this.ids = props.roomIds().stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }


    @Override
    public boolean existsById(UUID roomId) {
        return ids.contains(roomId);
    }
}
