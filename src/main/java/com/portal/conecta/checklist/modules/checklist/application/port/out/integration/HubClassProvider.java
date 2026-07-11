package com.portal.conecta.checklist.modules.checklist.application.port.out.integration;

import com.portal.conecta.checklist.modules.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.modules.checklist.domain.valueobject.ClassReference;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrato para validar turmas conhecidas pelo Hub.
 *
 * <p>Isola a aplicacao do contrato HTTP e permite que o adapter traduza os
 * dados retornados pelo Hub para referencias do dominio.</p>
 */
public interface HubClassProvider {

    boolean existsById(UUID classId);

    default Optional<ClassReference> findById(UUID classId) {
        return existsById(classId)
                ? Optional.of(new ClassReference(classId))
                : Optional.empty();
    }

    default Optional<Shift> findShiftByClassId(UUID classId) {
        return findById(classId).map((ClassReference classReference) -> classReference.getShift());
    }

    List<ClassReference> findByIds(List<UUID> classIds);
}
