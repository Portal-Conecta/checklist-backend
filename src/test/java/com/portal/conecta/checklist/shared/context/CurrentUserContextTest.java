package com.portal.conecta.checklist.shared.context;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentUserContextTest {

    @Test
    void representativeCanCreateChecklistForOwnClassOnly() {
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        CurrentUserContext user = new CurrentUserContext(
                userId,
                "Joao Silva",
                "joao@exemplo.com",
                "aluno",
                List.of(new CurrentUserClassLink(classId, null, "representante"))
        );

        assertThat(user.canCreateChecklistExecutionForClass(classId)).isTrue();
        assertThat(user.canCreateChecklistExecutionForClass(UUID.randomUUID())).isFalse();
    }

    @Test
    void regularStudentCannotCreateChecklistExecution() {
        UUID classId = UUID.randomUUID();
        CurrentUserContext user = new CurrentUserContext(
                UUID.randomUUID(),
                "Joao Silva",
                "joao@exemplo.com",
                "aluno",
                List.of(new CurrentUserClassLink(classId, "aluno", "aluno"))
        );

        assertThat(user.canCreateChecklistExecutionForClass(classId)).isFalse();
        assertThat(user.canAccessChecklistModule()).isFalse();
    }

    @Test
    void senaiAndWegProfilesCanManageTemplatesAndDashboards() {
        CurrentUserContext senai = new CurrentUserContext(UUID.randomUUID(), "Senai", "senai@exemplo.com", "perfil_senai");
        CurrentUserContext weg = new CurrentUserContext(UUID.randomUUID(), "Weg", "weg@exemplo.com", "perfil_weg");

        assertThat(senai.canManageChecklistTemplates()).isTrue();
        assertThat(senai.canViewDashboard()).isTrue();
        assertThat(weg.canManageChecklistTemplates()).isTrue();
        assertThat(weg.canViewDashboard()).isTrue();
    }
}
