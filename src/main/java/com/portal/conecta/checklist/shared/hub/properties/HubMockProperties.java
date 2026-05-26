package com.portal.conecta.checklist.shared.hub.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "checklist.mock.hub")
@EnableConfigurationProperties(HubMockProperties.class)
public record HubMockProperties(    List<String> classIds,
                                    List<String> userIds,
                                    List<String> roomIds) {



}
