package com.portal.conecta.checklist.shared.integration.hub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades de acesso a API do Hub.
 *
 * <p>Define a URL base usada pelos providers HTTP que consultam recursos
 * externos, como usuarios, salas e turmas.</p>
 */
@ConfigurationProperties(prefix = "hub.api")
public record HubApiProperties(String url) {

    public HubApiProperties {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("HUB_API_URL deve ser configurado.");
        }
    }
}
