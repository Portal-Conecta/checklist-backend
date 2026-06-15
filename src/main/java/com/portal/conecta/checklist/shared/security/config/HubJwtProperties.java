package com.portal.conecta.checklist.shared.security.config;

import io.jsonwebtoken.io.Decoders;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de configuracao do JWT para validacao dos tokens emitidos pelo Hub.
 *
 * <p>Lidas do prefixo {@code checklist.security.jwt} no {@code application.properties}.
 * * Em produção, o valor de {@code secret} deve ser fornecido via variável de ambiente
 * * {@code JWT_SECRET}, codificado em Base64 e com pelo menos 32 bytes ao decodificar.</p>
 * *
 * * @param secret segredo HMAC-SHA256 codificado em Base64 (minimo de 32 bytes decodificados)
 */

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
