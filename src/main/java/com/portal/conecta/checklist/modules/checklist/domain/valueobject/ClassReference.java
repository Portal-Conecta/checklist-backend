package com.portal.conecta.checklist.modules.checklist.domain.valueobject;

import com.portal.conecta.checklist.modules.checklist.domain.enums.Shift;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Value object que representa a referencia de uma turma externa.
 *
 * <p>O UUID e a unica informacao persistivel. Os demais campos sao dados reais
 * retornados pelo Hub e ficam apenas em memoria para enriquecer respostas,
 * validacoes e integracoes sem transformar o Checklist em fonte de verdade.</p>
 */
@Embeddable
@Getter
public class ClassReference {

    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Transient
    private String name;

    @Transient
    private Integer number;

    @Transient
    private Shift shift;

    @Transient
    private CourseReference courseReference;

    @Transient
    private Instant createdAt;

    protected ClassReference() {
    }

    public ClassReference(UUID classId) {
        this(classId, null, null, null, null, null);
    }

    public ClassReference(
            UUID classId,
            String name,
            Integer number,
            Shift shift,
            CourseReference courseReference,
            Instant createdAt
    ) {
        if (classId == null) {
            throw new IllegalArgumentException("O ID da turma esta nulo");
        }
        this.classId = classId;
        this.name = name;
        this.number = number;
        this.shift = shift;
        this.courseReference = courseReference;
        this.createdAt = createdAt;
    }
}
