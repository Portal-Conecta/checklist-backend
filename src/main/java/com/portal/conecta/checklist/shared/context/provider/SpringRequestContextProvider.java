package com.portal.conecta.checklist.shared.context.provider;

import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Provider que resolve o usuario autenticado a partir do Spring Security.
 *
 * <p>Le o principal salvo no {@code SecurityContextHolder} pelo filtro JWT e
 * entrega o {@link RequestContext} para os use cases.</p>
 */
@Component
public class SpringRequestContextProvider implements RequestContextProvider {

    @Override
    public RequestContext getRequestContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof RequestContext principal)) {
            throw new IllegalStateException("Usuario autenticado nao encontrado na requisicao atual.");
        }

        return principal;
    }
}
