package com.portal.conecta.checklist.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuracao compartilhada de clientes HTTP.
 *
 * <p>Centraliza a criacao de {@code RestClient.Builder} para providers que
 * precisam consumir APIs externas, como o Hub.</p>
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
