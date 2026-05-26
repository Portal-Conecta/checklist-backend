package com.portal.conecta.checklist.shared.hub.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "checklist.mock.hub")
public record HubMockProperties(    List<String> classIds,
                                    List<String> userIds,
                                    List<String> roomIds) {



}
