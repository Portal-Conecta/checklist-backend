package com.portal.conecta.checklist.shared.context;

import java.util.UUID;

/**
 * Representa o vinculo do usuario autenticado com uma turma.
 *
 * <p>Carrega o identificador da turma e o papel recebido no token do Hub para
 * apoiar regras locais de autorizacao.</p>
 */
public record ContextClass(UUID classId, String role) {

    public boolean matchesClass(UUID expectedClassId) {
        return classId != null && classId.equals(expectedClassId);
    }

    public boolean hasClassRole(String expectedClassRole) {
        return role != null && role.equalsIgnoreCase(expectedClassRole);
    }
}
