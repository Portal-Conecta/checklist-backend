package com.portal.conecta.checklist.unit.checklist.application.usecase.stats;

import com.portal.conecta.checklist.modules.checklist.application.dto.stats.DashboardStatsResponseDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query.ChecklistExecutionStatsUseCase;
import com.portal.conecta.checklist.modules.checklist.application.usecase.stats.ChecklistDashboardUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.query.ChecklistIssueStatsUseCase;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChecklistDashboardUseCaseTest {

    private final ChecklistExecutionStatsUseCase executionStats = mock(ChecklistExecutionStatsUseCase.class);
    private final ChecklistIssueStatsUseCase issueStats = mock(ChecklistIssueStatsUseCase.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final RequestContext context = mock(RequestContext.class);

    private final ChecklistDashboardUseCase useCase =
            new ChecklistDashboardUseCase(executionStats, issueStats, contextProvider);

    @BeforeEach
    void setUp() {
        when(contextProvider.getRequestContext()).thenReturn(context);
        when(context.canAccessChecklistModule()).thenReturn(true);
    }

    @Test
    @DisplayName("deve compor o dashboard orquestrando os stats granulares")
    void deveComporDashboard() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);
        when(executionStats.countByDay(from, to)).thenReturn(List.of(new StatsEntryDTO("2026-06-01", 3)));
        when(executionStats.countByStatus()).thenReturn(List.of(new StatsEntryDTO("SUBMITTED", 10)));
        when(executionStats.completionRate()).thenReturn(List.of(new StatsEntryDTO("submitted", 8)));
        when(issueStats.countByStatus()).thenReturn(List.of(new StatsEntryDTO("OPEN", 4)));
        when(issueStats.countByPriority()).thenReturn(List.of(new StatsEntryDTO("HIGH", 2)));
        when(issueStats.countByDay(from, to)).thenReturn(List.of(new StatsEntryDTO("2026-06-02", 1)));

        DashboardStatsResponseDTO result = useCase.execute(from, to);

        assertEquals(from, result.periodo().from());
        assertEquals(to, result.periodo().to());
        assertEquals("SUBMITTED", result.execucoesPorStatus().get(0).label());
        assertEquals("HIGH", result.issuesPorPrioridade().get(0).label());
        verify(executionStats).countByDay(from, to);
        verify(issueStats).countByDay(from, to);
    }

    @Test
    @DisplayName("datas nulas assumem os ultimos 30 dias")
    void deveAplicarDefaultDe30Dias() {
        useCase.execute(null, null);

        LocalDate hoje = LocalDate.now();
        verify(executionStats).countByDay(hoje.minusDays(30), hoje);
        verify(issueStats).countByDay(hoje.minusDays(30), hoje);
    }

    @Test
    @DisplayName("deve negar acesso sem permissao no modulo")
    void deveNegarSemPermissao() {
        when(context.canAccessChecklistModule()).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> useCase.execute(LocalDate.now().minusDays(5), LocalDate.now()));
    }

    @Test
    @DisplayName("deve rejeitar quando from e posterior a to")
    void deveRejeitarIntervaloInvertido() {
        LocalDate from = LocalDate.of(2026, 6, 30);
        LocalDate to = LocalDate.of(2026, 6, 1);

        assertThrows(InvalidRequestException.class, () -> useCase.execute(from, to));
    }
}
