package com.portal.conecta.checklist.unit.checklist.presentation.dto.execution.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;

class ChecklistExecutionDraftCreateDTOTest {

    @Test
    @DisplayName("payload de criacao de execucao nao deve receber periodo")
    void payloadDeCriacaoDeExecucaoNaoDeveReceberPeriod() {
        boolean hasPeriod = Arrays.stream(ChecklistExecutionDraftCreateDTO.class.getRecordComponents())
                .anyMatch(component -> component.getName().equals("period"));

        assertFalse(hasPeriod);
    }
}
