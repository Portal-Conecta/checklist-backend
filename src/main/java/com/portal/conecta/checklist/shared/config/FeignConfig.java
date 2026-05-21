package com.portal.conecta.checklist.shared.config;

import com.portal.conecta.checklist.shared.hub.client.HubRoomClient;
import com.portal.conecta.checklist.shared.hub.client.HubUserClient;
import com.portal.conecta.checklist.shared.hub.exception.HubIntegrationException;
import feign.Feign;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Value("${hub.api.url}")
    private String hubApiUrl;

    @Bean
    public HubRoomClient hubRoomClient() {
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .errorDecoder(hubErrorDecoder())
                .requestInterceptor(jwtRelayInterceptor())
                .target(HubRoomClient.class, hubApiUrl);
    }

    @Bean
    public HubUserClient hubUserClient() {
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .errorDecoder(hubErrorDecoder())
                .requestInterceptor(jwtRelayInterceptor())
                .target(HubUserClient.class, hubApiUrl);
    }

    @Bean
    public RequestInterceptor jwtRelayInterceptor() {
        return template -> {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String authHeader = attrs.getRequest().getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    template.header("Authorization", authHeader);
                }
            }
        };
    }

    @Bean
    public ErrorDecoder hubErrorDecoder() {
        return (methodKey, response) -> switch (response.status()) {
            case 404 -> new EntityNotFoundException("Recurso não encontrado no Hub: " + methodKey);
            case 401, 403 -> new AccessDeniedException("Sem permissão para acessar o Hub: " + methodKey);
            default -> new HubIntegrationException(
                    "Erro na integração com o Hub [" + response.status() + "]: " + methodKey);
        };
    }
}
