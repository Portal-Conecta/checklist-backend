package com.portal.conecta.checklist.shared.hub.provider.me;

import java.util.UUID;

/**
 * Contrato para validar o usuario autenticado por endpoints contextuais do Hub.
 */
public interface HubMeProvider {

    boolean existsAuthenticatedUser(UUID tokenUserId);
}
