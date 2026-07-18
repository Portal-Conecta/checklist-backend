package com.portal.conecta.checklist.module.checklist.domain.service;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.Period;
import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;

/**
 * Deriva o periodo da execucao a partir do turno da turma e do tipo de checklist.
 *
 * <p>Essa regra permanece no dominio porque nao depende de infraestrutura,
 * estado externo ou detalhes do fluxo de aplicacao.</p>
 */
public final class PeriodResolver {

    private PeriodResolver() {
    }

    public static Period resolve(Shift shift, ChecklistType checklistType) {
        return switch (shift) {
            case FULL_AM_PM -> switch (checklistType) {
                case ARRIVAL -> Period.MORNING;
                case POST_BREAK -> Period.AFTERNOON;
                default -> throw invalidCombination(shift, checklistType);
            };
            case FULL_PM_NT -> switch (checklistType) {
                case ARRIVAL -> Period.AFTERNOON;
                case POST_BREAK -> Period.NIGHT;
                default -> throw invalidCombination(shift, checklistType);
            };
        };
    }

    private static IllegalArgumentException invalidCombination(Shift shift, ChecklistType checklistType) {
        return new IllegalArgumentException(
                "Tipo de checklist invalido para o turno " + shift + ": " + checklistType
        );
    }
}
