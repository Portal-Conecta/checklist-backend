package com.portal.conecta.checklist.module.checklist.domain;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.Period;
import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;

/**
 * Deriva o Period a partir do turno real da turma e do tipo de checklist.
 * Elimina a dependencia do cliente declarar o period — prevenindo duplicatas silenciosas (RISK-001).
 */
public final class PeriodResolver {

    private PeriodResolver() {}

    public static Period resolve(Shift shift, ChecklistType checklistType) {
        return switch (shift) {
            case FULL_AM_PM -> switch (checklistType) {
                case ARRIVAL    -> Period.MORNING;
                case POST_BREAK -> Period.AFTERNOON;
                default -> throw new IllegalArgumentException(
                        "Tipo de checklist invalido para período " + shift + ": " + checklistType);
            };
            case FULL_PM_NT -> switch (checklistType) {
                case ARRIVAL    -> Period.AFTERNOON;
                case POST_BREAK -> Period.NIGHT;
                default -> throw new IllegalArgumentException(
                        "Tipo de checklist invalido para período " + shift + ": " + checklistType);
            };
        };
    }
}
