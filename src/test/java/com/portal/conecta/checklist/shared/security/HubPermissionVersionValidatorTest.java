package com.portal.conecta.checklist.shared.security;

import com.portal.conecta.checklist.shared.context.CurrentUserContext;
import com.portal.conecta.checklist.shared.hub.HubPermissionVersionClient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HubPermissionVersionValidatorTest {

    private final HubPermissionVersionClient permissionVersionClient = mock(HubPermissionVersionClient.class);
    private final HubPermissionVersionValidator validator = new HubPermissionVersionValidator(permissionVersionClient);

    @Test
    void shouldAllowWhenTokenPermissionVersionMatchesHubVersion() {
        UUID userId = UUID.randomUUID();
        CurrentUserContext currentUser = new CurrentUserContext(
                userId,
                null,
                null,
                "aluno",
                4,
                List.of()
        );

        when(permissionVersionClient.getPermissionVersion(userId)).thenReturn(4);

        validator.validate(currentUser);
    }

    @Test
    void shouldRejectWhenTokenPermissionVersionDiffersFromHubVersion() {
        UUID userId = UUID.randomUUID();
        CurrentUserContext currentUser = new CurrentUserContext(
                userId,
                null,
                null,
                "aluno",
                4,
                List.of()
        );

        when(permissionVersionClient.getPermissionVersion(userId)).thenReturn(5);

        assertThatThrownBy(() -> validator.validate(currentUser))
                .isInstanceOf(StalePermissionVersionException.class);
    }
}
