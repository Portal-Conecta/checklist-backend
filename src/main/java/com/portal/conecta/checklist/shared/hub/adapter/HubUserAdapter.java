package com.portal.conecta.checklist.shared.hub.adapter;

import com.portal.conecta.checklist.shared.hub.client.HubUserClient;
import com.portal.conecta.checklist.shared.hub.dto.UserDTO;
import com.portal.conecta.checklist.shared.hub.port.HubUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("!mock")
@RequiredArgsConstructor
public class HubUserAdapter implements HubUserPort {

    private final HubUserClient hubUserClient;

    @Override
    public UserDTO findById(UUID userId) {
        return hubUserClient.findById(userId);
    }
}
