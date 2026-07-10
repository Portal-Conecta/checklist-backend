package com.portal.conecta.checklist.unit.checklist.issues.application.usecase;

import com.portal.conecta.checklist.modules.checklist.issues.application.port.out.persistence.ChecklistIssueStatsPort;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.query.ChecklistIssueStatsUseCase;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChecklistIssueStatsUseCaseTest {

    private final ChecklistIssueStatsPort statsPort = mock(ChecklistIssueStatsPort.class);
    private final ChecklistIssueStatsUseCase useCase = new ChecklistIssueStatsUseCase(statsPort);

    @Test
    @DisplayName("countByDay com datas nulas deve usar padrÃ£o de 30 dias atrÃ¡s atÃ© hoje")
    void countByDayComDatasNulasDeveUsarPadrao() {
        when(statsPort.countByDay(any(), any())).thenReturn(List.of());

        useCase.countByDay(null, null);

        verify(statsPort).countByDay(
                LocalDate.now().minusDays(30),
                LocalDate.now()
        );
    }

    @Test
    @DisplayName("topFailingItems com limit nulo deve usar padrÃ£o 10")
    void topFailingItemsComLimitNuloDeveUsarPadrao() {
        when(statsPort.topFailingItems(10)).thenReturn(List.of());

        useCase.topFailingItems(null);

        verify(statsPort).topFailingItems(10);
    }

    @Test
    @DisplayName("topFailingItems com limit acima de 100 deve ser limitado a 100")
    void topFailingItemsComLimitAcimaDeMaxDeveSerLimitado() {
        when(statsPort.topFailingItems(100)).thenReturn(List.of());

        useCase.topFailingItems(500);

        verify(statsPort).topFailingItems(100);
    }

    @Test
    @DisplayName("topFailingItems com limit vÃ¡lido deve repassar ao port sem alteraÃ§Ã£o")
    void topFailingItemsComLimitValidoDeveRepassar() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("quadro", 50L));
        when(statsPort.topFailingItems(5)).thenReturn(expected);

        List<StatsEntryDTO> result = useCase.topFailingItems(5);

        assertEquals(expected, result);
        verify(statsPort).topFailingItems(5);
    }

    @Test
    @DisplayName("countByStatus deve delegar ao port diretamente")
    void countByStatusDeveDelegarAoPort() {
        List<StatsEntryDTO> expected = List.of(new StatsEntryDTO("OPEN", 45L));
        when(statsPort.countByStatus()).thenReturn(expected);

        List<StatsEntryDTO> result = useCase.countByStatus();

        assertEquals(expected, result);
        verify(statsPort).countByStatus();
    }
}
