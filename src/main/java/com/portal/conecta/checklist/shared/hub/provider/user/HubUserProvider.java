package com.portal.conecta.checklist.shared.hub.provider.user;

import java.util.UUID;

/**
 * Contrato para validar usuarios conhecidos pelo Hub.
 *
 * <p>Abstrai a origem da consulta de usuarios usada no fluxo de autenticacao
 * por token.</p>
 */
public interface HubUserProvider {

    boolean existsById(UUID userId);

}
