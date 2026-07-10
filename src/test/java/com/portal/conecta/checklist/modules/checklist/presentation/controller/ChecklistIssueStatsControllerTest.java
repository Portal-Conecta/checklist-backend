package com.portal.conecta.checklist.unit.checklist.presentation.controller;

import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.query.ChecklistIssueStatsUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.presentation.controller.ChecklistIssueStatsController;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.AvgResolutionTimeDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.IssuesPerExecutionDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.OverdueIssuesDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.ResolutionRateDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.ResolutionSplitDTO;
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

class ChecklistIssueStatsControllerTest {

    private final ChecklistIssueStatsUseCase statsUseCase = mock(ChecklistIssueStatsUseCase.class);
    private final ChecklistIssueStatsController controller =
            new ChecklistIssueStatsController(statsUseCase);

    // ────────────────────────────────────────────────────────────────────────
    // aggregate — dispatch por groupBy
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("aggregate groupBy=status deve retornar 200 com dados do usecase")
    void aggregateByStatusDeveRetornar200() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("OPEN", 45L));
        when(statsUseCase.countByStatus()).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.aggregate("status", null, null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).countByStatus();
    }

    @Test
    @DisplayName("aggregate groupBy=priority deve retornar 200 com dados do usecase")
    void aggregateByPriorityDeveRetornar200() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("HIGH", 20L));
        when(statsUseCase.countByPriority()).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.aggregate("priority", null, null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).countByPriority();
    }

    @Test
    @DisplayName("aggregate groupBy=day deve repassar from/to ao usecase")
    void aggregateByDayDeveRepassarFiltros() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("2026-06-10", 3L));
        when(statsUseCase.countByDay(from, to)).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.aggregate("day", from, to, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).countByDay(from, to);
    }

    @Test
    @DisplayName("aggregate groupBy=type deve retornar 200 com dados do usecase")
    void aggregateByTypeDeveRetornar200() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("ARRIVAL", 12L));
        when(statsUseCase.countByChecklistType()).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.aggregate("type", null, null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).countByChecklistType();
    }

    @Test
    @DisplayName("aggregate groupBy=item deve repassar limit ao usecase")
    void aggregateByItemDeveRepassarLimit() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("quadro", 50L));
        when(statsUseCase.topFailingItems(5)).thenReturn(expected);

        ResponseEntity<List<StatsEntryDTO>> result = controller.aggregate("item", null, null, 5);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).topFailingItems(5);
    }

    @Test
    @DisplayName("aggregate com groupBy inválido deve lançar IllegalArgumentException")
    void aggregateComGroupByInvalidoDeveLancarExcecao() {
        assertThrows(InvalidRequestException.class,
                () -> controller.aggregate("invalido", null, null, null));
    }

    // ────────────────────────────────────────────────────────────────────────
    // Sub-recursos
    // ────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("resolutionSplit deve retornar 200 com dados do usecase")
    void resolutionSplitDeveRetornar200() {
        ResolutionSplitDTO expected = ResolutionSplitDTO.of(45L, 83L);
        when(statsUseCase.resolutionSplit()).thenReturn(expected);

        ResponseEntity<ResolutionSplitDTO> result = controller.resolutionSplit();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).resolutionSplit();
    }

    @Test
    @DisplayName("resolutionRate deve retornar 200 com dados do usecase")
    void resolutionRateDeveRetornar200() {
        ResolutionRateDTO expected = ResolutionRateDTO.of(83L, 128L);
        when(statsUseCase.resolutionRate()).thenReturn(expected);

        ResponseEntity<ResolutionRateDTO> result = controller.resolutionRate();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).resolutionRate();
    }

    @Test
    @DisplayName("avgResolutionTime deve retornar 200 com dados do usecase")
    void avgResolutionTimeDeveRetornar200() {
        AvgResolutionTimeDTO expected = new AvgResolutionTimeDTO(86400.0);
        when(statsUseCase.avgResolutionTime()).thenReturn(expected);

        ResponseEntity<AvgResolutionTimeDTO> result = controller.avgResolutionTime();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).avgResolutionTime();
    }

    @Test
    @DisplayName("overdue deve retornar 200 com dados do usecase")
    void overdueDeveRetornar200() {
        OverdueIssuesDTO expected = new OverdueIssuesDTO(12L);
        when(statsUseCase.overdueCount()).thenReturn(expected);

        ResponseEntity<OverdueIssuesDTO> result = controller.overdue();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).overdueCount();
    }

    @Test
    @DisplayName("perExecution deve retornar 200 com dados do usecase")
    void perExecutionDeveRetornar200() {
        IssuesPerExecutionDTO expected = IssuesPerExecutionDTO.of(240L, 80L);
        when(statsUseCase.issuesPerExecution()).thenReturn(expected);

        ResponseEntity<IssuesPerExecutionDTO> result = controller.perExecution();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertSame(expected, result.getBody());
        verify(statsUseCase).issuesPerExecution();
    }
}
