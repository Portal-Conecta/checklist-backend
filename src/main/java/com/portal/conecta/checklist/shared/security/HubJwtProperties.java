package com.portal.conecta.checklist.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "checklist.security.jwt")
public record HubJwtProperties(String secret) {
}
