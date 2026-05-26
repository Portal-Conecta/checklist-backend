package com.portal.conecta.checklist.shared.hub.provider;

import java.util.UUID;

public interface HubUserProvider {

    boolean existsById(UUID userId);

}
