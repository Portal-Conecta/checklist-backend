package com.portal.conecta.checklist.shared.hub.provider.user;

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
public class HttpHubUserProvider implements HubUserProvider {

    private final RestClient restClient;

    public HttpHubUserProvider(HubApiProperties properties, RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(properties.url()).build();
    }

    @Override
    public boolean existsById(UUID userId) {
        try {
            restClient.get()
                    .uri("/users/{userId}", userId)
                    .retrieve()
                    .toBodilessEntity();

            return true;
        } catch (HttpClientErrorException.NotFound exception) {
            return false;
        } catch (RestClientException exception) {
            throw new HubIntegrationException("Servico de usuarios do Hub indisponivel.", exception);
        }
    }
}
