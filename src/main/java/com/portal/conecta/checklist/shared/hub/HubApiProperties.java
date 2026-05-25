package com.portal.conecta.checklist.shared.hub;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hub.api")
public record HubApiProperties(String url) {

    public HubApiProperties {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("HUB_API_URL must be configured.");
        }
    }
}
