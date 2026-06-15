package com.portal.conecta.checklist.shared.integration.hub.adapter.me;

import java.util.UUID;

/**
 * Contrato para validar o usuario autenticado no Hub usando endpoints contextuais.
 */
public interface HubMeProvider {

    boolean existsAuthenticatedUser(UUID tokenUserId);
}
