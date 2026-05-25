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
                "REPRESENTATIVE",
                List.of(new CurrentUserClassLink(classId, "REPRESENTATIVE"))
        );

        assertThat(user.canCreateChecklistExecutionForClass(classId)).isTrue();
        assertThat(user.canCreateChecklistExecutionForClass(UUID.randomUUID())).isFalse();
    }

    @Test
    void regularStudentCannotCreateChecklistExecution() {
        UUID classId = UUID.randomUUID();
        CurrentUserContext user = new CurrentUserContext(
                UUID.randomUUID(),
                "STUDENT",
                List.of(new CurrentUserClassLink(classId, "STUDENT"))
        );

        assertThat(user.canCreateChecklistExecutionForClass(classId)).isFalse();
        assertThat(user.canAccessChecklistModule()).isFalse();
    }

    @Test
    void teacherCanCreateChecklistForLinkedClassOnly() {
        UUID classId = UUID.randomUUID();
        CurrentUserContext user = new CurrentUserContext(
                UUID.randomUUID(),
                "TEACHER",
                List.of(new CurrentUserClassLink(classId, "TEACHER"))
        );

        assertThat(user.canCreateChecklistExecutionForClass(classId)).isTrue();
        assertThat(user.canCreateChecklistExecutionForClass(UUID.randomUUID())).isFalse();
    }

    @Test
    void senaiAndWegProfilesCanManageTemplatesAndDashboards() {
        CurrentUserContext senai = new CurrentUserContext(UUID.randomUUID(), "SENAI");
        CurrentUserContext weg = new CurrentUserContext(UUID.randomUUID(), "WEG");

        assertThat(senai.canManageChecklistTemplates()).isTrue();
        assertThat(senai.canViewDashboard()).isTrue();
        assertThat(weg.canManageChecklistTemplates()).isTrue();
        assertThat(weg.canViewDashboard()).isTrue();
    }
}
