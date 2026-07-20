package com.portal.conecta.checklist.unit.checklist.application.usecase.stats;

import com.portal.conecta.checklist.module.checklist.application.dto.stats.DashboardStatsResponseDTO;
import com.portal.conecta.checklist.module.checklist.application.usecase.stats.ChecklistDashboardLoader;
import com.portal.conecta.checklist.module.checklist.application.usecase.stats.ChecklistDashboardUseCase;
import com.portal.conecta.checklist.shared.context.RequestContext;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.exception.InvalidRequestException;
import com.portal.conecta.checklist.shared.stats.StatsEntryDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChecklistDashboardUseCaseTest {

    private final ChecklistDashboardLoader loader = mock(ChecklistDashboardLoader.class);
    private final RequestContextProvider contextProvider = mock(RequestContextProvider.class);
    private final RequestContext context = mock(RequestContext.class);

    private final ChecklistDashboardUseCase useCase =
            new ChecklistDashboardUseCase(loader, contextProvider);

    @BeforeEach
    void setUp() {
        when(contextProvider.getRequestContext()).thenReturn(context);
        when(context.canViewDashboard()).thenReturn(true);
    }

    @Test
    @DisplayName("deve compor o dashboard delegando a carga cacheada apos autorizar")
    void deveComporDashboard() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 6, 30);
        DashboardStatsResponseDTO payload = new DashboardStatsResponseDTO(
                new DashboardStatsResponseDTO.Periodo(from, to),
                List.of(new StatsEntryDTO("2026-06-01", 3)),
                List.of(new StatsEntryDTO("SUBMITTED", 10)),
                List.of(new StatsEntryDTO("submitted", 8)),
                List.of(new StatsEntryDTO("OPEN", 4)),
                List.of(new StatsEntryDTO("HIGH", 2)),
                List.of(new StatsEntryDTO("2026-06-02", 1))
        );
        when(loader.load(from, to)).thenReturn(payload);

        DashboardStatsResponseDTO result = useCase.execute(from, to);

        assertEquals(from, result.periodo().from());
        assertEquals(to, result.periodo().to());
        assertEquals("SUBMITTED", result.execucoesPorStatus().get(0).label());
        assertEquals("HIGH", result.issuesPorPrioridade().get(0).label());
        verify(loader).load(from, to);
    }

    @Test
    @DisplayName("datas nulas assumem os ultimos 30 dias")
    void deveAplicarDefaultDe30Dias() {
        LocalDate hoje = LocalDate.now();
        LocalDate from = hoje.minusDays(30);
        when(loader.load(from, hoje)).thenReturn(new DashboardStatsResponseDTO(
                new DashboardStatsResponseDTO.Periodo(from, hoje),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of()
        ));

        useCase.execute(null, null);

        verify(loader).load(from, hoje);
    }

    @Test
    @DisplayName("deve negar acesso sem permissao e nao consultar o cache/loader")
    void deveNegarSemPermissao() {
        when(context.canViewDashboard()).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> useCase.execute(LocalDate.now().minusDays(5), LocalDate.now()));

        verify(loader, never()).load(any(), any());
    }

    @Test
    @DisplayName("deve rejeitar quando from e posterior a to")
    void deveRejeitarIntervaloInvertido() {
        LocalDate from = LocalDate.of(2026, 6, 30);
        LocalDate to = LocalDate.of(2026, 6, 1);

        assertThrows(InvalidRequestException.class, () -> useCase.execute(from, to));
        verify(loader, never()).load(any(), any());
    }
}
