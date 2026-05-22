package com.portal.conecta.checklist.module.checklist.infrastructure.client.hub;

import java.util.UUID;

public interface HubRoomClient {

    boolean existsById(UUID roomId);
}
