package com.portal.conecta.checklist.module.checklist.domain;

import com.portal.conecta.checklist.module.checklist.domain.enums.ChecklistType;
import com.portal.conecta.checklist.module.checklist.domain.enums.Period;
import com.portal.conecta.checklist.module.checklist.domain.enums.Shift;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistExecutionDraftCreateDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PeriodResolverTest {

    @Test
    @DisplayName("deve derivar periodo pelo turno e tipo de checklist")
    void deveDerivarPeriodoPeloTurnoETipoDeChecklist() {
        assertEquals(Period.MORNING, PeriodResolver.resolve(Shift.FULL_AM_PM, ChecklistType.ARRIVAL));
        assertEquals(Period.AFTERNOON, PeriodResolver.resolve(Shift.FULL_AM_PM, ChecklistType.POST_BREAK));
        assertEquals(Period.AFTERNOON, PeriodResolver.resolve(Shift.FULL_PM_NT, ChecklistType.ARRIVAL));
        assertEquals(Period.NIGHT, PeriodResolver.resolve(Shift.FULL_PM_NT, ChecklistType.POST_BREAK));
    }

    @Test
    @DisplayName("payload de criacao de execucao nao deve receber period")
    void payloadDeCriacaoDeExecucaoNaoDeveReceberPeriod() {
        boolean hasPeriod = Arrays.stream(ChecklistExecutionDraftCreateDTO.class.getRecordComponents())
                .anyMatch(component -> component.getName().equals("period"));

        assertFalse(hasPeriod);
    }
}
