package com.portal.conecta.checklist.shared.hub.provider.user;

import java.util.UUID;

public interface HubUserProvider {

    boolean existsById(UUID userId);

}
