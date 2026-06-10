package com.portal.conecta.checklist.shared.context;

import java.util.UUID;

/**
 * Representa o vinculo do usuario autenticado com uma turma.
 *
 * <p>Carrega o identificador da turma e o papel recebido no token do Hub para
 * apoiar regras locais de autorizacao.</p>
 */
public record ContextClass(UUID classId, ClassRole role) {

    public boolean matchesClass(UUID expectedClassId) {
        return classId != null && classId.equals(expectedClassId);
    }

    public boolean hasRole(ClassRole expectedRole) {
        return role != null && role == expectedRole;
    }
}
