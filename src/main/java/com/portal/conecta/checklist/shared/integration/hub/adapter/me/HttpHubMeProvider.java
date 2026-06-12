package com.portal.conecta.checklist.shared.integration.hub.adapter.me;

import com.portal.conecta.checklist.shared.integration.hub.client.me.HubMeClient;
import com.portal.conecta.checklist.shared.integration.hub.exception.HubIntegrationException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Provider HTTP que valida o usuario autenticado pelo endpoint contextual do Hub.
 *
 * <p>O Hub identifica o usuario pelo bearer token repassado via Feign, portanto o
 * identificador do token nao e enviado na URL.</p>
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
            throw new HubIntegrationException("Servico de contexto do usuario autenticado no Hub indisponivel.", exception);
        }
    }
}
