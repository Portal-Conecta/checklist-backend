package com.portal.conecta.checklist.shared.hub.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "checklist.mock.hub")
public record HubMockProperties(
        List<String> classIds,
        List<String> userIds,
        List<String> roomIds
) {

    public HubMockProperties {
        classIds = classIds == null ? List.of() : List.copyOf(classIds);
        userIds = userIds == null ? List.of() : List.copyOf(userIds);
        roomIds = roomIds == null ? List.of() : List.copyOf(roomIds);
    }
}
