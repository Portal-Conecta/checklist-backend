package com.portal.conecta.checklist.shared.context;

import java.util.UUID;

public record CurrentUserClassLink(
        UUID id,
        String relacao,
        String papelNaTurma
) {

    public boolean matchesClass(UUID classId) {
        return id != null && id.equals(classId);
    }

    public boolean hasRelation(String expectedRelation) {
        return relacao != null && relacao.equalsIgnoreCase(expectedRelation);
    }

    public boolean hasClassRole(String expectedClassRole) {
        return papelNaTurma != null && papelNaTurma.equalsIgnoreCase(expectedClassRole);
    }
}
