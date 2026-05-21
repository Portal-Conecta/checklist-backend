package com.portal.conecta.checklist.module.checklist.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.UUID;

@Embeddable
@Getter
public class ClassReference {

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    protected ClassReference() {
    }

    public ClassReference(UUID classId) {
        if (classId == null) {
            throw new IllegalArgumentException("O ID da turma esta nulo");
        }
        this.classId = classId;
    }
}
