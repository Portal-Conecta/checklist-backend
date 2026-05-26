package com.portal.conecta.checklist.shared.hub.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@ConfigurationProperties(prefix = "checklist.mock.hub")
public record HubMockProperties(    List<String> classIds,
                                    List<String> userIds,
                                    List<String> roomIds) {



}
