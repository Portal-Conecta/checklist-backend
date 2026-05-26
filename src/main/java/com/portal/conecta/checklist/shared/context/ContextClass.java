package com.portal.conecta.checklist.shared.context;

import java.util.UUID;

public record ContextClass(UUID classId, String role) {

    public boolean matchesClass(UUID expectedClassId) {
        return classId != null && classId.equals(expectedClassId);
    }

    public boolean hasClassRole(String expectedClassRole) {
        return role != null && role.equalsIgnoreCase(expectedClassRole);
    }
}
