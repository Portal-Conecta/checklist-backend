package com.portal.conecta.checklist.shared.context;

import java.util.List;
import java.util.UUID;

/**
 * Contexto do usuario autenticado usado pelas regras de autorizacao.
 *
 * <p>Concentra o usuario, perfil global e vinculos com turmas recebidos do
 * token do Hub, oferecendo metodos de permissao para o modulo Checklist.</p>
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
        return userType == TypeUser.SENAI || userType == TypeUser.WEG || userType == TypeUser.ADMIN;
    }

    public boolean canViewDashboard() {
        return canManageChecklistTemplates();
    }

    public boolean canManageIssues() {
        return canManageChecklistTemplates();
    }

    public boolean canOnlySenaiManageIssues() {
        return userType == TypeUser.SENAI || userType == TypeUser.ADMIN;
    }

    public boolean canEditCompletedChecklist() {
        return canViewDashboard();
    }

    /**
     * Cancelamento segue a mesma regra de criar/submeter: qualquer representante
     * ou professor vinculado à turma (ou gestor SENAI/WEG/ADMIN), sem exigir ownership
     * de quem criou o rascunho.
     */
    public boolean canCancelChecklistExecution(UUID classId) {
        if (canManageChecklistTemplates()) {
            return true;
        }

        return canOperateChecklistExecutionForClass(classId);
    }

    public boolean canOperateChecklistExecutionForClass(UUID classId) {
        // ADMIN tem bypass direto, não precisa de vínculo na lista 'classes'
        if (userType == TypeUser.ADMIN) {
            return true;
        }

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
