package com.portal.conecta.checklist.shared.hub.mock;

import com.portal.conecta.checklist.shared.hub.dto.UserDTO;
import com.portal.conecta.checklist.shared.hub.port.HubUserPort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@Profile("mock")
public class HubUserMock implements HubUserPort {

    private static final Map<UUID, UserDTO> USERS = Map.of(
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
            new UserDTO(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                    "Kael", "kael@conecta.com", null, "REPRESENTANTE"),

            UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
            new UserDTO(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                    "Murilo", "murilo@conecta.com", null, "DOCENTE"),

            UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
            new UserDTO(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"),
                    "Daniel", "daniel@conecta.com", null, "ADMINISTRADOR")
    );

    @Override
    public UserDTO findById(UUID userId) {
        UserDTO user = USERS.get(userId);
        if (user == null) {
            throw new EntityNotFoundException("Usuário não encontrado (mock): " + userId);
        }
        return user;
    }
}
