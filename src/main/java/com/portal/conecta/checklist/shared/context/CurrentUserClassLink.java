package com.portal.conecta.checklist.shared.context;

public record CurrentUserClassLink(
        String id,
        String relacao,
        String papelNaTurma
) {

    public boolean matchesClass(String classId) {
        return id != null && id.equalsIgnoreCase(classId);
    }

    public boolean hasRelation(String expectedRelation) {
        return relacao != null && relacao.equalsIgnoreCase(expectedRelation);
    }

    public boolean hasClassRole(String expectedClassRole) {
        return papelNaTurma != null && papelNaTurma.equalsIgnoreCase(expectedClassRole);
    }
}
