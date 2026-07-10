package com.portal.conecta.checklist.unit.checklist.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.application.usecase.template.query.ChecklistTemplateStatsUseCase;
import com.portal.conecta.checklist.modules.checklist.presentation.controller.ChecklistTemplateStatsController;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.StatsEntryDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChecklistTemplateStatsControllerTest {

    private final ChecklistTemplateStatsUseCase statsUseCase = mock(ChecklistTemplateStatsUseCase.class);
    private final ChecklistTemplateStatsController controller =
            new ChecklistTemplateStatsController(statsUseCase);

    @Test
    @DisplayName("aggregate groupBy=status deve retornar 200 com dados do usecase")
    void aggregateByStatusDeveRetornar200() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("ACTIVE", 5L));
        when(statsUseCase.countByStatus()).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.aggregate("status");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).countByStatus();
    }

    @Test
    @DisplayName("aggregate groupBy=active deve retornar 200 com dados do usecase")
    void aggregateByActiveDeveRetornar200() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("true", 4L), new StatsEntryDTO("false", 2L));
        when(statsUseCase.countByActive()).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.aggregate("active");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).countByActive();
    }

    @Test
    @DisplayName("aggregate groupBy=day deve retornar 200 com dados do usecase")
    void aggregateByDayDeveRetornar200() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("2026-06-01", 2L));
        when(statsUseCase.countByDay()).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.aggregate("day");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).countByDay();
    }

    @Test
    @DisplayName("aggregate groupBy=group deve retornar 200 com dados do usecase")
    void aggregateByGroupDeveRetornar200() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("uuid-group-1", 3L));
        when(statsUseCase.countVersionsByGroup()).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.aggregate("group");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).countVersionsByGroup();
    }

    @Test
    @DisplayName("aggregate com groupBy inválido deve lançar IllegalArgumentException")
    void aggregateComGroupByInvalidoDeveLancarExcecao() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.aggregate("invalido"));
    }
}
