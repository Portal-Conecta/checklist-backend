package com.portal.conecta.checklist.unit.checklist.application.usecase.execution;

import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistExecutionStatsPort;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query.ChecklistExecutionStatsUseCase;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.CompletionRateDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.WithIssuesRateDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChecklistExecutionStatsUseCaseTest {

    private final ChecklistExecutionStatsPort statsPort = mock(ChecklistExecutionStatsPort.class);
    private final ChecklistExecutionStatsUseCase useCase = new ChecklistExecutionStatsUseCase(statsPort);

    @Test
    @DisplayName("countByDay com datas nulas deve usar padrão de 30 dias atrás até hoje")
    void countByDayComDatasNulasDeveUsarPadrao() {
        when(statsPort.countByDay(any(), any())).thenReturn(List.of());

        useCase.countByDay(null, null);

        verify(statsPort).countByDay(
                LocalDate.now().minusDays(30),
                LocalDate.now()
        );
    }

    @Test
    @DisplayName("countByDay com datas explícitas deve repassar ao port sem alteração")
    void countByDayComDatasExplicitasDeveRepassar() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("2026-06-01", 5L));
        when(statsPort.countByDay(from, to)).thenReturn(expected);

        List<StatsEntryDTO> result = useCase.countByDay(from, to);

        assertEquals(expected, result);
        verify(statsPort).countByDay(from, to);
    }

    @Test
    @DisplayName("completionRate deve delegar ao port e retornar o resultado")
    void completionRateDeveDelegarAoPort() {
        CompletionRateDTO expected = CompletionRateDTO.of(128L, 160L);
        when(statsPort.completionRate()).thenReturn(expected);

        CompletionRateDTO result = useCase.completionRate();

        assertNotNull(result);
        assertEquals(expected, result);
        verify(statsPort).completionRate();
    }

    @Test
    @DisplayName("withIssuesRate deve delegar ao port e retornar o resultado")
    void withIssuesRateDeveDelegarAoPort() {
        WithIssuesRateDTO expected = WithIssuesRateDTO.of(52L, 128L);
        when(statsPort.withIssuesRate()).thenReturn(expected);

        WithIssuesRateDTO result = useCase.withIssuesRate();

        assertNotNull(result);
        assertEquals(expected, result);
        verify(statsPort).withIssuesRate();
    }

    @Test
    @DisplayName("countByStatus deve delegar ao port diretamente")
    void countByStatusDeveDelegarAoPort() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("SUBMITTED", 100L));
        when(statsPort.countByStatus()).thenReturn(expected);

        List<StatsEntryDTO> result = useCase.countByStatus();

        assertEquals(expected, result);
        verify(statsPort).countByStatus();
    }
}
