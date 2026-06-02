package com.portal.conecta.checklist.shared.hub.provider.user;

import com.portal.conecta.checklist.module.checklist.domain.valueobject.UserReference;

import java.util.Optional;
import java.util.UUID;

/**
 * Contrato para validar usuarios conhecidos pelo Hub.
 *
 * <p>Abstrai a origem da consulta de usuarios usada no fluxo de autenticacao
 * por token.</p>
 */
public interface HubUserProvider {

    boolean existsById(UUID userId);

    default Optional<UserReference> findById(UUID userId) {
        return existsById(userId)
                ? Optional.of(new UserReference(userId))
                : Optional.empty();
    }
}
