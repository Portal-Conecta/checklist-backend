package com.portal.conecta.checklist.shared.context;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("mock")
public class MockCurrentUserProvider implements CurrentUserProvider {

    private final UUID id;
    private final String name;
    private final String email;
    private final String profile;

    public MockCurrentUserProvider(
            @Value("${checklist.mock.current-user.id:44444444-4444-4444-4444-444444444444}") String id,
            @Value("${checklist.mock.current-user.name:Coordenador SENAI}") String name,
            @Value("${checklist.mock.current-user.email:coordenador.senai@exemplo.com}") String email,
            @Value("${checklist.mock.current-user.profile:PERFIL_SENAI}") String profile
    ) {
        this.id = UUID.fromString(id);
        this.name = name;
        this.email = email;
        this.profile = profile;
    }

    @Override
    public CurrentUserContext getCurrentUser() {
        return new CurrentUserContext(id, name, email, profile);
    }
}
