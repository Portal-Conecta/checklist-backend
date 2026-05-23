package com.portal.conecta.checklist.shared.context;

import java.util.List;
import java.util.UUID;

public record CurrentUserContext(
        UUID id,
        String name,
        String email,
        String profile,
        List<CurrentUserClassLink> classes
) {

    public CurrentUserContext(UUID id, String name, String email, String profile) {
        this(id, name, email, profile, List.of());
    }

    public CurrentUserContext {
        classes = classes == null ? List.of() : List.copyOf(classes);
    }

    public boolean canAccessChecklistModule() {
        return !"APRENDIZ".equals(profile);
    }

    public boolean canManageChecklistTemplates() {
        return "PERFIL_SENAI".equals(profile) || "PERFIL_WEG".equals(profile);
    }

    public boolean canViewDashboard() {
        return "PERFIL_SENAI".equals(profile) || "PERFIL_WEG".equals(profile);
    }

    public boolean canEditCompletedChecklist() {
        return canViewDashboard();
    }

    public boolean canCreateChecklistExecutionForClass(UUID classId) {
        if (classId == null) {
            return false;
        }

        String expectedClassId = classId.toString();

        return classes.stream().anyMatch(classLink ->
                classLink.matchesClass(expectedClassId)
                        && (
                        isClassRepresentative(classLink)
                                || isLinkedTeacher(classLink)
                )
        );
    }

    private boolean isClassRepresentative(CurrentUserClassLink classLink) {
        return classLink.hasRelation("aluno")
                && classLink.hasClassRole("representante");
    }

    private boolean isLinkedTeacher(CurrentUserClassLink classLink) {
        return classLink.hasRelation("docente")
                || classLink.hasClassRole("docente")
                || classLink.hasClassRole("professor");
    }
}
