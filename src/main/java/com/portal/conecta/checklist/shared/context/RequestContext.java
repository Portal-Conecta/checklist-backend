package com.portal.conecta.checklist.shared.context;

import java.util.List;
import java.util.UUID;

/**
 * Contexto do usuario autenticado usado pelas regras de autorizacao.
 *
 * <p>Concentra o usuario, perfil global e vinculos com turmas recebidos do
 * token do Hub, oferecendo metodos de permissao para o modulo Checklist.</p>
 *
 * <p>{@link TypeUser#ADMIN} tem o mesmo poder gerencial de {@link TypeUser#SENAI}
 * e {@link TypeUser#WEG} (templates, dashboard, issues, cancelamento e consulta
 * de qualquer turma). Nao opera fluxo de rascunho/submissao como representante
 * ou professor — isso permanece restrito a perfis operacionais com vinculo de turma.</p>
 */
public record RequestContext(
        UUID userId,
        TypeUser userType,
        List<ContextClass> classes
) {

    public RequestContext {
        classes = classes == null ? List.of() : List.copyOf(classes);
    }

    public RequestContext(UUID userId, TypeUser userType) {
        this(userId, userType, List.of());
    }

    public boolean canAccessChecklistModule() {
        return canManageChecklistTemplates()
                || hasOperationalProfile() && classes.stream().anyMatch(c -> canActAsRepresentative(c) || canActAsTeacher(c));
    }

    public boolean canManageChecklistTemplates() {
        return userType == TypeUser.SENAI
                || userType == TypeUser.WEG
                || userType == TypeUser.ADMIN;
    }

    public boolean canViewDashboard() {
        return canManageChecklistTemplates();
    }

    public boolean canManageIssues() {
        return canManageChecklistTemplates();
    }

    public boolean canValidateOrReopenIssues() {
        return canManageChecklistTemplates();
    }

    public boolean canEditCompletedChecklist() {
        return canViewDashboard();
    }

    public boolean canCreateChecklistExecutionForClass(UUID classId) {
        return canOperateChecklistExecutionForClass(classId);
    }

    public boolean canSubmitChecklistExecutionForClass(UUID classId) {
        return canOperateChecklistExecutionForClass(classId);
    }

    public boolean canCancelChecklistExecution(UUID classId) {
        if (canManageChecklistTemplates()) {
            return true;
        }

        return canOperateChecklistExecutionForClass(classId);
    }

    /**
     * Operacao de execucao (criar rascunho / submeter) exige perfil operacional
     * com vinculo na turma. Gestores (SENAI/WEG/ADMIN) nao passam por este caminho.
     */
    public boolean canOperateChecklistExecutionForClass(UUID classId) {
        if (classId == null) {
            return false;
        }

        return classes.stream().anyMatch(c ->
                c.matchesClass(classId) && (canActAsRepresentative(c) || canActAsTeacher(c))
        );
    }

    private boolean hasOperationalProfile() {
        return userType == TypeUser.STUDENT
                || userType == TypeUser.REPRESENTATIVE
                || userType == TypeUser.TEACHER;
    }

    private boolean canActAsRepresentative(ContextClass contextClass) {
        return (userType == TypeUser.STUDENT || userType == TypeUser.REPRESENTATIVE)
                && isClassRepresentative(contextClass);
    }

    private boolean canActAsTeacher(ContextClass contextClass) {
        return userType == TypeUser.TEACHER && isLinkedTeacher(contextClass);
    }

    private boolean isClassRepresentative(ContextClass contextClass) {
        return contextClass.hasRole(ClassRole.REPRESENTATIVE);
    }

    private boolean isLinkedTeacher(ContextClass contextClass) {
        return contextClass.hasRole(ClassRole.TEACHER);
    }
}
