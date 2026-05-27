package com.portal.conecta.checklist.shared.hub.provider.room;

import java.util.UUID;

public interface HubRoomProvider {

    boolean existsById(UUID roomId);
}
