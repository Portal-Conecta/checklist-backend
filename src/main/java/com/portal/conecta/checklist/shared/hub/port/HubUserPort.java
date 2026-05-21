package com.portal.conecta.checklist.shared.hub.port;

import com.portal.conecta.checklist.shared.hub.dto.UserDTO;

import java.util.UUID;

public interface HubUserPort {

    UserDTO findById(UUID userId);
}
