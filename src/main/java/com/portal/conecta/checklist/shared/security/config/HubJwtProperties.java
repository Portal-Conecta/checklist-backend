package com.portal.conecta.checklist.shared.security.config;

import io.jsonwebtoken.io.Decoders;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "checklist.security.jwt")
public record HubJwtProperties(String secret) {

    public HubJwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET deve ser configurado.");
        }

        byte[] decodedSecret;

        try {
            decodedSecret = Decoders.BASE64.decode(secret);
        } catch (RuntimeException exception) {
            throw new IllegalStateException("JWT_SECRET deve estar codificado em Base64.", exception);
        }

        if (decodedSecret.length < 32) {
            throw new IllegalStateException("JWT_SECRET deve decodificar para pelo menos 32 bytes para HS256.");
        }
    }
}
