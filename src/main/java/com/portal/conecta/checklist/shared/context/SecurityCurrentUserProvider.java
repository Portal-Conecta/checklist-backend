package com.portal.conecta.checklist.shared.context;

import com.portal.conecta.checklist.shared.security.HubUserPrincipal;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Profile("!mock")
public class SecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public CurrentUserContext getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof HubUserPrincipal principal)) {
            throw new IllegalStateException("Usuario autenticado nao encontrado na requisicao atual.");
        }

        return new CurrentUserContext(
                principal.id(),
                principal.name(),
                principal.email(),
                principal.profile(),
                principal.classes()
        );
    }
}
