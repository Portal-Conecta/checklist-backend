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
 * existe e esta ativo na fonte central da plataforma.</p>
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

            if (response == null || !response.active()) {
                return Optional.empty();
            }

            UUID resolvedUserId = response.id() == null ? userId : response.id();
            return Optional.of(new UserReference(resolvedUserId));
        } catch (FeignException.NotFound | FeignException.Unauthorized | FeignException.Forbidden exception) {
            return Optional.empty();
        } catch (FeignException exception) {
            throw new HubIntegrationException("Servico de usuarios do Hub indisponivel.", exception);
        }
    }
}
