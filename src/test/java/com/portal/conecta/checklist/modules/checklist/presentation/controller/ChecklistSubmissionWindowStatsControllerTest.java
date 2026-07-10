package com.portal.conecta.checklist.unit.checklist.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.application.usecase.window.query.ChecklistSubmissionWindowStatsUseCase;
import com.portal.conecta.checklist.modules.checklist.presentation.controller.ChecklistSubmissionWindowStatsController;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.AvgFillTimeEntryDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.portal.conecta.checklist.shared.exception.InvalidRequestException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChecklistSubmissionWindowStatsControllerTest {

    private final ChecklistSubmissionWindowStatsUseCase statsUseCase =
            mock(ChecklistSubmissionWindowStatsUseCase.class);
    private final ChecklistSubmissionWindowStatsController controller =
            new ChecklistSubmissionWindowStatsController(statsUseCase);

    @Test
    @DisplayName("aggregate groupBy=type deve retornar 200 com dados do usecase")
    void aggregateByTypeDeveRetornar200() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("ARRIVAL", 3L));
        when(statsUseCase.countByType()).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.aggregate("type");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).countByType();
    }

    @Test
    @DisplayName("aggregate groupBy=shift deve retornar 200 com dados do usecase")
    void aggregateByShiftDeveRetornar200() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("MORNING", 2L));
        when(statsUseCase.countByShift()).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.aggregate("shift");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).countByShift();
    }

    @Test
    @DisplayName("aggregate com groupBy invÃ¡lido deve lanÃ§ar IllegalArgumentException")
    void aggregateComGroupByInvalidoDeveLancarExcecao() {
        assertThrows(InvalidRequestException.class,
                () -> controller.aggregate("invalido"));
    }

    @Test
    @DisplayName("avgDuration deve retornar 200 com dados do usecase")
    void avgDurationDeveRetornar200() {
        List<AvgFillTimeEntryDTO> expected = List.of(new AvgFillTimeEntryDTO("ARRIVAL", 45.0));
        when(statsUseCase.avgDurationByType()).thenReturn(expected);

        ResponseEntity<List<AvgFillTimeEntryDTO>> result = controller.avgDuration();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).avgDurationByType();
    }
}
