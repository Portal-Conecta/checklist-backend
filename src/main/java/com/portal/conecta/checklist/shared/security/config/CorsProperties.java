package com.portal.conecta.checklist.shared.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Propriedades de configuração de CORS para o Checklist.
 *
 * <p>Lidas do prefixo {@code checklist.cors} no {@code application.yml}.
 * Em produção, as origens permitidas devem ser fornecidas via variável de ambiente
 * {@code CHECKLIST_CORS_ALLOWED_ORIGINS}, separadas por vírgula.</p>
 *
 * @param allowedOrigins lista de origens (protocol + domínio + porta) autorizadas a fazer requisições cross-origin
 */
@ConfigurationProperties(prefix = "checklist.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            throw new IllegalStateException(
                    "checklist.cors.allowed-origins deve conter pelo menos uma origem permitida."
            );
        }
    }
}
