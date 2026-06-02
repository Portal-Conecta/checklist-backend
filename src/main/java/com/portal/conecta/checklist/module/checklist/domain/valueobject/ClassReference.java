package com.portal.conecta.checklist.module.checklist.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.UUID;

/**
 * Value object que representa a referencia de uma turma externa.
 *
 * <p>Encapsula o identificador vindo do Hub e impede a criacao de referencias
 * sem UUID valido.</p>
 */
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
