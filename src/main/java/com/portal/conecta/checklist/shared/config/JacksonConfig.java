package com.portal.conecta.checklist.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracao compartilhada do Jackson.
 *
 * <p>Disponibiliza customizacoes de serializacao usadas pela API para lidar
 * com tipos e formatos comuns nas respostas JSON.</p>
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
