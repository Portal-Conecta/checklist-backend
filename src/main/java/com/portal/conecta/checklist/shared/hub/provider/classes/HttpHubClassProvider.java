package com.portal.conecta.checklist.shared.hub.provider.classes;

import com.portal.conecta.checklist.shared.hub.exception.HubIntegrationException;
import com.portal.conecta.checklist.shared.hub.properties.HubApiProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
@Profile("!mock & !test")
public class HttpHubClassProvider implements HubClassProvider {

    private final RestClient restClient;

    public HttpHubClassProvider(HubApiProperties properties, RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(properties.url()).build();
    }

    @Override
    public boolean existsById(UUID classId) {
        try {
            restClient.get()
                    .uri("/classes/{classId}", classId)
                    .retrieve()
                    .toBodilessEntity();

            return true;
        } catch (HttpClientErrorException.NotFound exception) {
            return false;
        } catch (RestClientException exception) {
            throw new HubIntegrationException("Servico de turmas do Hub indisponivel.", exception);
        }
    }
}
