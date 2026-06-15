package com.portal.conecta.checklist.modules.checklist.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.Getter;

import java.util.UUID;

/**
 * Value object que representa a referencia de um curso externo.
 *
 * <p>O UUID e o unico dado persistivel. Nome e codigo sao metadados retornados
 * pelo Hub para uso em respostas e validacoes, mantendo o Hub como fonte de
 * verdade dos dados cadastrais.</p>
 */
@Embeddable
@Getter
public class CourseReference {

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Transient
    private String name;

    @Transient
    private String code;

    protected CourseReference() {
    }

    public CourseReference(UUID courseId) {
        this(courseId, null, null);
    }

    public CourseReference(UUID courseId, String name, String code) {
        if (courseId == null) {
            throw new IllegalArgumentException("O ID do curso esta nulo");
        }
        this.courseId = courseId;
        this.name = name;
        this.code = code;
    }
}
