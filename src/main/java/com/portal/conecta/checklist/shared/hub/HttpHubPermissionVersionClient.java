package com.portal.conecta.checklist.shared.hub;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Component
@Profile("!mock & !test")
public class HttpHubPermissionVersionClient implements HubPermissionVersionClient {

    private final RestClient restClient;

    public HttpHubPermissionVersionClient(HubApiProperties properties, RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl(properties.url()).build();
    }

    @Override
    public int getPermissionVersion(UUID userId) {
        try {
            PermissionVersionResponse response = restClient.get()
                    .uri("/users/{userId}/permission-version", userId)
                    .retrieve()
                    .body(PermissionVersionResponse.class);

            if (response == null || response.version() == null) {
                throw new HubIntegrationException("Hub permission version response is invalid.");
            }

            return response.version();
        } catch (RestClientException exception) {
            throw new HubIntegrationException("Hub permission version service is unavailable.", exception);
        }
    }

    private record PermissionVersionResponse(Integer version) {
    }
}
