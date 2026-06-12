package com.portal.conecta.checklist.shared.hub.provider.me;

import com.portal.conecta.checklist.shared.hub.client.me.HubMeClient;
import com.portal.conecta.checklist.shared.hub.exception.HubIntegrationException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Valida o usuario autenticado por meio do endpoint contextual do Hub.
 */
@Component
@Profile("!mock & !test")
@RequiredArgsConstructor
public class HttpHubMeProvider implements HubMeProvider {

    private final HubMeClient hubMeClient;

    @Override
    public boolean existsAuthenticatedUser(UUID tokenUserId) {
        try {
            hubMeClient.findMyCourses();
            return true;
        } catch (FeignException.NotFound | FeignException.Unauthorized | FeignException.Forbidden exception) {
            return false;
        } catch (FeignException exception) {
            throw new HubIntegrationException(
                    "Servico de contexto do usuario autenticado no Hub indisponivel.",
                    exception
            );
        }
    }
}
