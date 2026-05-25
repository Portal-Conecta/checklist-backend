package com.portal.conecta.checklist.shared.context;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Profile({"mock", "test"})
public class MockCurrentUserProvider implements CurrentUserProvider {

    private final UUID id;
    private final String name;
    private final String email;
    private final String profile;
    private final int permissionVersion;

    public MockCurrentUserProvider(
            @Value("${checklist.mock.current-user.id:44444444-4444-4444-4444-444444444444}") String id,
            @Value("${checklist.mock.current-user.name:Coordenador SENAI}") String name,
            @Value("${checklist.mock.current-user.email:coordenador.senai@exemplo.com}") String email,
            @Value("${checklist.mock.current-user.profile:PERFIL_SENAI}") String profile,
            @Value("${checklist.mock.current-user.permission-version:1}") int permissionVersion
    ) {
        this.id = UUID.fromString(id);
        this.name = name;
        this.email = email;
        this.profile = profile;
        this.permissionVersion = permissionVersion;
    }

    @Override
    public CurrentUserContext getCurrentUser() {
        return new CurrentUserContext(id, name, email, profile, permissionVersion, List.of());
    }
}
