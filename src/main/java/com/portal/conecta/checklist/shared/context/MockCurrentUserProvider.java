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
    private final String userType;

    public MockCurrentUserProvider(
            @Value("${checklist.mock.current-user.id:44444444-4444-4444-4444-444444444444}") String id,
            @Value("${checklist.mock.current-user.type:SENAI}") String userType
    ) {
        this.id = UUID.fromString(id);
        this.userType = userType;
    }

    @Override
    public CurrentUserContext getCurrentUser() {
        return new CurrentUserContext(id, userType, List.of());
    }
}
