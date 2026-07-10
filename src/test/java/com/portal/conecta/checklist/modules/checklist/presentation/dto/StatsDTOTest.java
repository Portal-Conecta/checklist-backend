package com.portal.conecta.checklist.unit.checklist.presentation.dto;

import com.portal.conecta.checklist.modules.checklist.application.dto.stats.CompletionRateDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.IssuesPerExecutionDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.ResolutionRateDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.ResolutionSplitDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.WithIssuesRateDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatsDTOTest {

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // CompletionRateDTO
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Test
    @DisplayName("CompletionRateDTO.of deve calcular percentual corretamente")
    void completionRateDtoDeveCalcularPercentual() {
        CompletionRateDTO dto = CompletionRateDTO.of(128L, 160L);
        assertEquals(128L, dto.submitted());
        assertEquals(160L, dto.total());
        assertEquals(80.0, dto.ratePercent(), 0.01);
    }

    @Test
    @DisplayName("CompletionRateDTO.of com total zero deve retornar 0%")
    void completionRateDtoComTotalZeroDeveRetornarZero() {
        CompletionRateDTO dto = CompletionRateDTO.of(0L, 0L);
        assertEquals(0.0, dto.ratePercent());
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // ResolutionSplitDTO
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Test
    @DisplayName("ResolutionSplitDTO.of deve somar open + resolved em total")
    void resolutionSplitDtoDeveCalcularTotal() {
        ResolutionSplitDTO dto = ResolutionSplitDTO.of(45L, 83L);
        assertEquals(45L, dto.open());
        assertEquals(83L, dto.resolved());
        assertEquals(128L, dto.total());
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // ResolutionRateDTO
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Test
    @DisplayName("ResolutionRateDTO.of deve calcular percentual corretamente")
    void resolutionRateDtoDeveCalcularPercentual() {
        ResolutionRateDTO dto = ResolutionRateDTO.of(83L, 128L);
        assertEquals(83L, dto.resolved());
        assertEquals(128L, dto.total());
        assertEquals(64.84, dto.ratePercent(), 0.01);
    }

    @Test
    @DisplayName("ResolutionRateDTO.of com total zero deve retornar 0%")
    void resolutionRateDtoComTotalZeroDeveRetornarZero() {
        ResolutionRateDTO dto = ResolutionRateDTO.of(0L, 0L);
        assertEquals(0.0, dto.ratePercent());
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // WithIssuesRateDTO
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Test
    @DisplayName("WithIssuesRateDTO.of deve calcular percentual corretamente")
    void withIssuesRateDtoDeveCalcularPercentual() {
        WithIssuesRateDTO dto = WithIssuesRateDTO.of(52L, 128L);
        assertEquals(52L, dto.executionsWithIssues());
        assertEquals(128L, dto.totalExecutions());
        assertEquals(40.63, dto.ratePercent(), 0.01);
    }

    @Test
    @DisplayName("WithIssuesRateDTO.of com total zero deve retornar 0%")
    void withIssuesRateDtoComTotalZeroDeveRetornarZero() {
        WithIssuesRateDTO dto = WithIssuesRateDTO.of(0L, 0L);
        assertEquals(0.0, dto.ratePercent());
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // IssuesPerExecutionDTO
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @Test
    @DisplayName("IssuesPerExecutionDTO.of deve calcular mÃ©dia corretamente")
    void issuesPerExecutionDtoDeveCalcularMedia() {
        IssuesPerExecutionDTO dto = IssuesPerExecutionDTO.of(240L, 80L);
        assertEquals(240L, dto.totalIssues());
        assertEquals(80L, dto.totalExecutions());
        assertEquals(3.0, dto.avgPerExecution(), 0.01);
    }

    @Test
    @DisplayName("IssuesPerExecutionDTO.of com executions zero deve retornar mÃ©dia 0")
    void issuesPerExecutionDtoComZeroDeveRetornarZero() {
        IssuesPerExecutionDTO dto = IssuesPerExecutionDTO.of(0L, 0L);
        assertEquals(0.0, dto.avgPerExecution());
    }
}
