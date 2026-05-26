package com.portal.conecta.checklist.shared.security.config;

import io.jsonwebtoken.io.Decoders;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "checklist.security.jwt")
public record HubJwtProperties(String secret) {

    public HubJwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be configured.");
        }

        byte[] decodedSecret;

        try {
            decodedSecret = Decoders.BASE64.decode(secret);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("JWT_SECRET must be Base64 encoded.", exception);
        }

        if (decodedSecret.length < 32) {
            throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes for HS256.");
        }
    }
}
