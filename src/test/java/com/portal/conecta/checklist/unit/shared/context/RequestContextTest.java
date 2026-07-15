package com.portal.conecta.checklist.unit.shared.context;

import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.TypeUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestContextTest {

    @Test
    @DisplayName("ADMIN com classes vazio no JWT deve conseguir acessar módulo, gerenciar templates, ver dashboard e gerenciar issues")
    void adminShouldHaveFullAccessToModuleAndTemplates() {
        RequestContext context = new RequestContext(UUID.randomUUID(), TypeUser.ADMIN, List.of());

        assertTrue(context.canAccessChecklistModule(), "ADMIN deve conseguir acessar o módulo");
        assertTrue(context.canManageChecklistTemplates(), "ADMIN deve conseguir gerenciar templates e janelas");
        assertTrue(context.canViewDashboard(), "ADMIN deve conseguir visualizar o dashboard");
        assertTrue(context.canManageIssues(), "ADMIN deve conseguir gerenciar issues em nível geral");
        assertTrue(context.canEditCompletedChecklist(), "ADMIN deve conseguir editar checklist concluído");
    }

    @Test
    @DisplayName("ADMIN deve conseguir operar checklist (criar/editar/submeter) para QUALQUER turma, ignorando o classId")
    void adminShouldOperateChecklistExecutionBypassingClassRequirement() {
        RequestContext context = new RequestContext(UUID.randomUUID(), TypeUser.ADMIN, List.of());
        UUID randomClassId = UUID.randomUUID();

        assertTrue(context.canOperateChecklistExecutionForClass(randomClassId),
            "ADMIN não deve sofrer bloqueio de vínculo de turma (bypass de classId)");

        assertTrue(context.canOperateChecklistExecutionForClass(null),
            "ADMIN não deve sofrer bloqueio de vínculo de turma mesmo com classId null");
    }

    @Test
    @DisplayName("ADMIN deve conseguir cancelar execução de qualquer turma")
    void adminShouldCancelChecklistExecution() {
        RequestContext context = new RequestContext(UUID.randomUUID(), TypeUser.ADMIN, List.of());
        UUID randomClassId = UUID.randomUUID();

        assertTrue(context.canCancelChecklistExecution(randomClassId),
            "ADMIN deve conseguir cancelar execução de qualquer turma");
    }

    @Test
    @DisplayName("ADMIN deve ter privilégios equivalentes ao SENAI para transições críticas de issues (validar/reabrir)")
    void adminShouldBeAbleToManageSenaiOnlyIssues() {
        RequestContext context = new RequestContext(UUID.randomUUID(), TypeUser.ADMIN, List.of());

        assertTrue(context.canOnlySenaiManageIssues(),
            "ADMIN deve passar na validação que antes era exclusiva para SENAI (ex: validar/reabrir issues)");
    }

    @Test
    @DisplayName("Garante que estudante NÃO tem bypass e é barrado em permissões administrativas")
    void ensureOperationalProfilesAreNotAdmins() {
        RequestContext context = new RequestContext(UUID.randomUUID(), TypeUser.STUDENT, List.of());

        assertFalse(context.canManageChecklistTemplates());
        assertFalse(context.canOnlySenaiManageIssues());
        assertFalse(context.canOperateChecklistExecutionForClass(UUID.randomUUID()),
            "Estudante SEM a turma na lista 'classes' deve ser barrado.");
    }
}
