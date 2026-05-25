package com.portal.conecta.checklist.shared.context;

import java.util.List;
import java.util.Locale;
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

    public CurrentUserContext(UUID id, String name, String email, String profile, List<CurrentUserClassLink> classes) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profile = profile;
        this.classes = classes == null ? List.of() : List.copyOf(classes);
    }

    public boolean canAccessChecklistModule() {
        return canManageChecklistTemplates()
                || classes.stream().anyMatch(classLink -> isClassRepresentative(classLink) || isLinkedTeacher(classLink));
    }

    public boolean canManageChecklistTemplates() {
        return hasProfile("PERFIL_SENAI") || hasProfile("PERFIL_WEG");
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

        return classes.stream().anyMatch(classLink ->
                classLink.matchesClass(classId)
                        && (
                        isClassRepresentative(classLink)
                                || isLinkedTeacher(classLink)
                )
        );
    }

    private boolean isClassRepresentative(CurrentUserClassLink classLink) {
        return classLink.hasClassRole("representante")
                && (
                classLink.hasRelation("aluno")
                        || hasProfile("ALUNO")
                        || hasProfile("REPRESENTANTE")
        );
    }

    private boolean isLinkedTeacher(CurrentUserClassLink classLink) {
        return hasProfile("DOCENTE")
                || classLink.hasRelation("docente")
                || classLink.hasClassRole("docente")
                || classLink.hasClassRole("professor");
    }

    private boolean hasProfile(String expectedProfile) {
        return normalize(profile).equals(normalize(expectedProfile));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim()
                .replace('-', '_')
                .toUpperCase(Locale.ROOT);
    }
}
