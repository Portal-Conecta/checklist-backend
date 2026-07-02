package com.portal.conecta.checklist.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    public Clock clock(@Value("${checklist.timezone:America/Sao_Paulo}") String timezone) {
        return Clock.system(ZoneId.of(timezone));
    }
}
