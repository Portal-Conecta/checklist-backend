package com.portal.conecta.checklist.modules.checklist.issues.application.usecase.stats;

import com.portal.conecta.checklist.modules.checklist.issues.application.port.out.persistence.ChecklistIssueStatsPort;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.*;
import com.portal.conecta.checklist.shared.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChecklistIssueStatsUseCase {

    private final ChecklistIssueStatsPort checklistIssueStatsPort;

    public List<StatsEntryDTO> countByDay(LocalDate from, LocalDate to) {
        // Defense-in-depth validation
        validateDateRange(from, to);
        return checklistIssueStatsPort.countByDay(from, to);
    }

    public List<StatsEntryDTO> countByStatus() {
        return checklistIssueStatsPort.countByStatus();
    }

    public List<StatsEntryDTO> countByPriority() {
        return checklistIssueStatsPort.countByPriority();
    }

    public List<StatsEntryDTO> countByChecklistType() {
        return checklistIssueStatsPort.countByChecklistType();
    }

    public ResolutionSplitDTO resolutionSplit() {
        return checklistIssueStatsPort.resolutionSplit();
    }

    public ResolutionRateDTO resolutionRate() {
        return checklistIssueStatsPort.resolutionRate();
    }

    public AvgResolutionTimeDTO avgResolutionTime() {
        return checklistIssueStatsPort.avgResolutionTime();
    }

    public OverdueIssuesDTO overdueCount() {
        return checklistIssueStatsPort.overdueCount();
    }

    public IssuesPerExecutionDTO issuesPerExecution() {
        return checklistIssueStatsPort.issuesPerExecution();
    }

    public List<StatsEntryDTO> topFailingItems(Integer limit) {
        // Defense-in-depth validation
        validateLimit(limit);
        return checklistIssueStatsPort.topFailingItems(limit);
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

    private void validateLimit(Integer limit) {
        if (limit == null) {
            throw new IllegalArgumentException("Limit parameter is required");
        }

        if (limit < 1) {
            throw new InvalidRequestException("Limit parameter must be greater than or equal to 1");
        }
    }
}