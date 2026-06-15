package com.portal.conecta.checklist.modules.checklist.application.port.out.integration;

import com.portal.conecta.checklist.modules.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.modules.checklist.domain.valueobject.ClassReference;

import java.util.Optional;
import java.util.UUID;

/**
 * Contrato para validar turmas conhecidas pelo Hub.
 *
 * <p>Isola a aplicacao da origem dos dados, permitindo usar implementacoes HTTP
 * em producao e mocks em testes.</p>
 */
public interface HubClassProvider {

    boolean existsById(UUID classId);

    default Optional<ClassReference> findById(UUID classId) {
        return existsById(classId)
                ? Optional.of(new ClassReference(classId))
                : Optional.empty();
    }

    default Optional<Shift> findShiftByClassId(UUID classId) {
        return findById(classId).map(ClassReference::getShift);
    }
}
