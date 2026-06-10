package com.portal.conecta.checklist.shared.context;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RequestContextTest {

    @Test
    void representativeCanCreateChecklistForOwnClassOnly() {
        UUID userId = UUID.randomUUID();
        UUID classId = UUID.randomUUID();
        RequestContext user = new RequestContext(
                userId,
                TypeUser.REPRESENTATIVE,
                List.of(new ContextClass(classId, ClassRole.REPRESENTATIVE))
        );

        assertThat(user.canCreateChecklistExecutionForClass(classId)).isTrue();
        assertThat(user.canOperateChecklistExecutionForClass(classId)).isTrue();
        assertThat(user.canSubmitChecklistExecutionForClass(classId)).isTrue();
        assertThat(user.canCancelChecklistExecution(userId, classId)).isTrue();
        assertThat(user.canCancelChecklistExecution(UUID.randomUUID(), classId)).isFalse();
        assertThat(user.canCreateChecklistExecutionForClass(UUID.randomUUID())).isFalse();
    }

    @Test
    void regularStudentCannotCreateChecklistExecution() {
        UUID classId = UUID.randomUUID();
        RequestContext user = new RequestContext(
                UUID.randomUUID(),
                TypeUser.STUDENT,
                List.of(new ContextClass(classId, ClassRole.STUDENT))
        );

        assertThat(user.canCreateChecklistExecutionForClass(classId)).isFalse();
        assertThat(user.canAccessChecklistModule()).isFalse();
    }

    @Test
    void teacherCanCreateChecklistForLinkedClassOnly() {
        UUID classId = UUID.randomUUID();
        RequestContext user = new RequestContext(
                UUID.randomUUID(),
                TypeUser.TEACHER,
                List.of(new ContextClass(classId, ClassRole.TEACHER))
        );

        assertThat(user.canCreateChecklistExecutionForClass(classId)).isTrue();
        assertThat(user.canOperateChecklistExecutionForClass(classId)).isTrue();
        assertThat(user.canSubmitChecklistExecutionForClass(classId)).isTrue();
        assertThat(user.canCancelChecklistExecution(user.userId(), classId)).isTrue();
        assertThat(user.canCreateChecklistExecutionForClass(UUID.randomUUID())).isFalse();
    }

    @Test
    void senaiAndWegProfilesCanManageTemplatesAndDashboards() {
        RequestContext senai = new RequestContext(UUID.randomUUID(), TypeUser.SENAI);
        RequestContext weg = new RequestContext(UUID.randomUUID(), TypeUser.WEG);

        assertThat(senai.canManageChecklistTemplates()).isTrue();
        assertThat(senai.canViewDashboard()).isTrue();
        assertThat(senai.canCancelChecklistExecution(UUID.randomUUID(), UUID.randomUUID())).isTrue();
        assertThat(weg.canManageChecklistTemplates()).isTrue();
        assertThat(weg.canViewDashboard()).isTrue();
        assertThat(weg.canCancelChecklistExecution(UUID.randomUUID(), UUID.randomUUID())).isTrue();
    }

    @Test
    void managementProfilesCannotCreateChecklistExecutionEvenWithClassRole() {
        UUID classId = UUID.randomUUID();
        RequestContext senai = new RequestContext(
                UUID.randomUUID(),
                TypeUser.SENAI,
                List.of(new ContextClass(classId, ClassRole.TEACHER))
        );
        RequestContext weg = new RequestContext(
                UUID.randomUUID(),
                TypeUser.WEG,
                List.of(new ContextClass(classId, ClassRole.REPRESENTATIVE))
        );
        RequestContext admin = new RequestContext(
                UUID.randomUUID(),
                TypeUser.ADMIN,
                List.of(new ContextClass(classId, ClassRole.TEACHER))
        );

        assertThat(senai.canCreateChecklistExecutionForClass(classId)).isFalse();
        assertThat(senai.canOperateChecklistExecutionForClass(classId)).isFalse();
        assertThat(senai.canSubmitChecklistExecutionForClass(classId)).isFalse();
        assertThat(weg.canCreateChecklistExecutionForClass(classId)).isFalse();
        assertThat(weg.canOperateChecklistExecutionForClass(classId)).isFalse();
        assertThat(weg.canSubmitChecklistExecutionForClass(classId)).isFalse();
        assertThat(admin.canCreateChecklistExecutionForClass(classId)).isFalse();
        assertThat(admin.canOperateChecklistExecutionForClass(classId)).isFalse();
        assertThat(admin.canSubmitChecklistExecutionForClass(classId)).isFalse();
        assertThat(admin.canCancelChecklistExecution(admin.userId(), classId)).isFalse();
        assertThat(admin.canAccessChecklistModule()).isFalse();
    }
}
