package com.portal.conecta.checklist.modules.checklist.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query.ChecklistExecutionStatsUseCase;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import com.portal.conecta.checklist.shared.exception.InvalidRequestException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChecklistExecutionStatsControllerTest {

    private final ChecklistExecutionStatsUseCase statsUseCase = mock(ChecklistExecutionStatsUseCase.class);
    private final ChecklistExecutionStatsController controller =
        new ChecklistExecutionStatsController(statsUseCase);

    // ────────────────────────────────────────────────────────────────────────
    // aggregate — dispatch por groupBy
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("aggregate groupBy=status deve retornar 200 com dados do usecase")
    void aggregateByStatusDeveRetornar200() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("SUBMITTED", 10L));
        when(statsUseCase.countByStatus()).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.aggregate("status", null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).countByStatus();
    }

    @Test
    @DisplayName("aggregate groupBy=day deve repassar from/to ao usecase")
    void aggregateByDayDeveRepassarFiltros() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("2026-06-01", 5L));
        when(statsUseCase.countByDay(from, to)).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.aggregate("day", from, to);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).countByDay(from, to);
    }

    @Test
    @DisplayName("aggregate groupBy=type deve retornar 200 com dados do usecase")
    void aggregateByTypeDeveRetornar200() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("ARRIVAL", 20L));
        when(statsUseCase.countByType()).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.aggregate("type", null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).countByType();
    }

    @Test
    @DisplayName("aggregate groupBy=shift deve retornar 200 com dados do usecase")
    void aggregateByShiftDeveRetornar200() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("MORNING", 30L));
        when(statsUseCase.countByShift()).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.aggregate("shift", null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).countByShift();
    }

    @Test
    @DisplayName("aggregate groupBy=period deve retornar 200 com dados do usecase")
    void aggregateByPeriodDeveRetornar200() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("MORNING", 15L));
        when(statsUseCase.countByPeriod()).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.aggregate("period", null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).countByPeriod();
    }

    @Test
    @DisplayName("aggregate groupBy=day+status deve repassar from/to ao usecase")
    void aggregateByDayAndStatusDeveRepassarFiltros() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("2026-06-01|SUBMITTED", 8L));
        when(statsUseCase.countByDayAndStatus(from, to)).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.aggregate("day+status", from, to);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).countByDayAndStatus(from, to);
    }

    @Test
    @DisplayName("aggregate com groupBy inválido deve lançar IllegalArgumentException")
    void aggregateComGroupByInvalidoDeveLancarExcecao() {
        assertThrows(InvalidRequestException.class,
            () -> controller.aggregate("invalido", null, null));
    }

    // ────────────────────────────────────────────────────────────────────────
    // Sub-recursos
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("completionRate deve retornar 200 com dados do usecase")
    void completionRateDeveRetornar200() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("completed", 128L));
        when(statsUseCase.completionRate()).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.completionRate();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).completionRate();
    }

    @Test
    @DisplayName("avgFillTime deve retornar 200 com dados do usecase")
    void avgFillTimeDeveRetornar200() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("2026-06-01", 245L));
        when(statsUseCase.avgFillTimeByDay(from, to)).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.avgFillTime(from, to);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).avgFillTimeByDay(from, to);
    }

    @Test
    @DisplayName("withIssuesRate deve retornar 200 com dados do usecase")
    void withIssuesRateDeveRetornar200() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("with_issues", 52L));
        when(statsUseCase.withIssuesRate()).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.withIssuesRate();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).withIssuesRate();
    }

    @Test
    @DisplayName("heatmap deve retornar 200 com dados do usecase")
    void heatmapDeveRetornar200() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("MORNING|1", 34L));
        when(statsUseCase.heatmap()).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.heatmap();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).heatmap();
    }
}
