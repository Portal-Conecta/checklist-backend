package com.portal.conecta.checklist.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "checklist.security")
public record ChecklistSecurityProperties(boolean swaggerPublic) {
}
