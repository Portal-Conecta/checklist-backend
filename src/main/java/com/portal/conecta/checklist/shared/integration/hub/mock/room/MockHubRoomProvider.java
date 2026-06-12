package com.portal.conecta.checklist.shared.integration.hub.mock.room;

import com.portal.conecta.checklist.shared.integration.hub.config.HubMockProperties;
import com.portal.conecta.checklist.modules.checklist.application.port.out.integration.HubRoomProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
/**
 * Provider mockado de salas do Hub.
 *
 * <p>Permite validar salas em ambiente local sem depender do servico real do
 * Hub.</p>
 */
@Component
@Profile({"mock", "test"})
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
