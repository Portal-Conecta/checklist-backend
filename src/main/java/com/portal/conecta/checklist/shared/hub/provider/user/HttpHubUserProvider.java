package com.portal.conecta.checklist.shared.hub.provider.user;

import com.portal.conecta.checklist.module.checklist.domain.valueobject.UserReference;
import com.portal.conecta.checklist.shared.hub.client.user.HubUserClient;
import com.portal.conecta.checklist.shared.hub.client.user.HubUserResponse;
import com.portal.conecta.checklist.shared.hub.exception.HubIntegrationException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Provider HTTP para consultar usuarios no Hub real.
 *
 * <p>Usado na validacao do token para garantir que o usuario autenticado ainda
 * existe na fonte central da plataforma.</p>
 */
@Component
@Profile("!mock & !test")
@RequiredArgsConstructor
public class HttpHubUserProvider implements HubUserProvider {

    private final HubUserClient hubUserClient;

    @Override
    public boolean existsById(UUID userId) {
        return findById(userId).isPresent();
    }

    @Override
    public Optional<UserReference> findById(UUID userId) {
        try {
            HubUserResponse response = hubUserClient.findById(userId);

            return response == null ? Optional.empty() : Optional.of(response.toReference(userId));
        } catch (FeignException.NotFound exception) {
            return Optional.empty();
        } catch (FeignException exception) {
            throw new HubIntegrationException("Servico de usuarios do Hub indisponivel.", exception);
        }
    }
}
