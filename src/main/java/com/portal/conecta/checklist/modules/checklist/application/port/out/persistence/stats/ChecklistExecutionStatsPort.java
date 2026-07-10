package com.portal.conecta.checklist.modules.checklist.application.port.out.persistence;

import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.AvgFillTimeEntryDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.CompletionRateDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.HeatmapEntryDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.StatsEntryDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.WithIssuesRateDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Port for retrieving statistics about checklist executions.
 */
public interface ChecklistExecutionStatsPort {

    List<StatsEntryDTO> countByDay(LocalDate from, LocalDate to);

    List<StatsEntryDTO> countByStatus();

    List<StatsEntryDTO> countByType();

    List<StatsEntryDTO> countByShift();

    List<StatsEntryDTO> countByPeriod();

    CompletionRateDTO completionRate();

    List<AvgFillTimeEntryDTO> avgFillTimeByDay(LocalDate from, LocalDate to);

    List<StatsEntryDTO> countByDayAndStatus(LocalDate from, LocalDate to);

    WithIssuesRateDTO withIssuesRate();

    List<HeatmapEntryDTO> heatmapShiftByDayOfWeek();
}