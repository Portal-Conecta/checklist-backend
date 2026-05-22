package com.portal.conecta.checklist.shared.context;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockCurrentUserProvider implements CurrentUserProvider {

    @Override
    public CurrentUserContext getCurrentUser() {
        return new CurrentUserContext(
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                "Coordenador SENAI",
                "coordenador.senai@exemplo.com",
                "PERFIL_SENAI"
        );
    }
}
