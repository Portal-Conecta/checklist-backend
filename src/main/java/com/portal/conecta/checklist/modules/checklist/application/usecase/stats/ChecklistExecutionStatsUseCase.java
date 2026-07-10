package com.portal.conecta.checklist.modules.checklist.application.usecase.stats;

import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistExecutionStatsPort;
import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistSubmissionWindowStatsPort;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.*;
import com.portal.conecta.checklist.shared.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChecklistExecutionStatsUseCase {

    private final ChecklistExecutionStatsPort checklistExecutionStatsPort;
    private final ChecklistSubmissionWindowStatsPort checklistSubmissionWindowStatsPort;

    public List<StatsEntryDTO> countByDay(LocalDate from, LocalDate to) {
        // Defense-in-depth validation
        validateDateRange(from, to);
        return checklistExecutionStatsPort.countByDay(from, to);
    }

    public List<StatsEntryDTO> countByStatus() {
        return checklistExecutionStatsPort.countByStatus();
    }

    public List<StatsEntryDTO> countByType() {
        return checklistExecutionStatsPort.countByType();
    }

    public List<StatsEntryDTO> countByShift() {
        return checklistExecutionStatsPort.countByShift();
    }

    public List<StatsEntryDTO> countByPeriod() {
        return checklistExecutionStatsPort.countByPeriod();
    }

    public CompletionRateDTO completionRate() {
        return checklistExecutionStatsPort.completionRate();
    }

    public List<AvgFillTimeEntryDTO> avgFillTimeByDay(LocalDate from, LocalDate to) {
        // Defense-in-depth validation
        validateDateRange(from, to);
        return checklistExecutionStatsPort.avgFillTimeByDay(from, to);
    }

    public List<StatsEntryDTO> countByDayAndStatus(LocalDate from, LocalDate to) {
        // Defense-in-depth validation
        validateDateRange(from, to);
        return checklistExecutionStatsPort.countByDayAndStatus(from, to);
    }

    public WithIssuesRateDTO withIssuesRate() {
        return checklistExecutionStatsPort.withIssuesRate();
    }

    public List<HeatmapEntryDTO> heatmapShiftByDayOfWeek() {
        return checklistExecutionStatsPort.heatmapShiftByDayOfWeek();
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("From and To dates are required");
        }

        if (from.isAfter(to)) {
            throw new InvalidRequestException("'from' date must be before or equal to 'to' date");
        }

        // Maximum 2-year interval
        if (from.plusYears(2).isBefore(to)) {
            throw new InvalidRequestException("Date range cannot exceed 2 years");
        }

        // No future dates
        if (to.isAfter(LocalDate.now())) {
            throw new InvalidRequestException("'to' date cannot be in the future");
        }
    }
}