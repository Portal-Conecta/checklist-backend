package com.portal.conecta.checklist.shared.context;

import java.util.List;
import java.util.UUID;

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
                || classes.stream().anyMatch(c -> isClassRepresentative(c) || isLinkedTeacher(c));
    }

    public boolean canManageChecklistTemplates() {
        return userType == TypeUser.SENAI || userType == TypeUser.WEG;
    }

    public boolean canViewDashboard() {
        return canManageChecklistTemplates();
    }

    public boolean canEditCompletedChecklist() {
        return canViewDashboard();
    }

    public boolean canCreateChecklistExecutionForClass(UUID classId) {
        if (classId == null) {
            return false;
        }

        if (hasManagementProfile()) {
            return false;
        }

        return classes.stream().anyMatch(c ->
                c.matchesClass(classId) && (isClassRepresentative(c) || isLinkedTeacher(c))
        );
    }

    private boolean hasManagementProfile() {
        return userType == TypeUser.SENAI || userType == TypeUser.WEG || userType == TypeUser.ADMIN;
    }

    private boolean isClassRepresentative(ContextClass contextClass) {
        return contextClass.hasClassRole("REPRESENTATIVE");
    }

    private boolean isLinkedTeacher(ContextClass contextClass) {
        return contextClass.hasClassRole("TEACHER");
    }
}
