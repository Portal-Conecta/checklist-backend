package com.portal.conecta.checklist.modules.checklist.issues.presentation.controller.stats;

import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.stats.ChecklistIssueStatsUseCase;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.*;
import com.portal.conecta.checklist.shared.exception.InvalidRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stats/issues")
@RequiredArgsConstructor
@Tag(name = "Issue Statistics", description = "Endpoints for issue statistics")
public class ChecklistIssueStatsController {

    private final ChecklistIssueStatsUseCase checklistIssueStatsUseCase;

    @GetMapping("/count-by-day")
    @Operation(summary = "Get issue count by day", description = "Returns the count of issues grouped by day for the specified period")
    public ResponseEntity<List<StatsEntryDTO>> countByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        // Validate date range
        validateDateRange(from, to);

        return ResponseEntity.ok(checklistIssueStatsUseCase.countByDay(from, to));
    }

    @GetMapping("/count-by-status")
    @Operation(summary = "Get issue count by status", description = "Returns the count of issues grouped by status")
    public ResponseEntity<List<StatsEntryDTO>> countByStatus() {
        return ResponseEntity.ok(checklistIssueStatsUseCase.countByStatus());
    }

    @GetMapping("/count-by-priority")
    @Operation(summary = "Get issue count by priority", description = "Returns the count of issues grouped by priority")
    public ResponseEntity<List<StatsEntryDTO>> countByPriority() {
        return ResponseEntity.ok(checklistIssueStatsUseCase.countByPriority());
    }

    @GetMapping("/count-by-checklist-type")
    @Operation(summary = "Get issue count by checklist type", description = "Returns the count of issues grouped by checklist type")
    public ResponseEntity<List<StatsEntryDTO>> countByChecklistType() {
        return ResponseEntity.ok(checklistIssueStatsUseCase.countByChecklistType());
    }

    @GetMapping("/resolution-split")
    @Operation(summary = "Get issue resolution split", description = "Returns the split of issue resolutions")
    public ResponseEntity<ResolutionSplitDTO> resolutionSplit() {
        return ResponseEntity.ok(checklistIssueStatsUseCase.resolutionSplit());
    }

    @GetMapping("/resolution-rate")
    @Operation(summary = "Get issue resolution rate", description = "Returns the rate of issue resolutions")
    public ResponseEntity<ResolutionRateDTO> resolutionRate() {
        return ResponseEntity.ok(checklistIssueStatsUseCase.resolutionRate());
    }

    @GetMapping("/avg-resolution-time")
    @Operation(summary = "Get average resolution time", description = "Returns the average resolution time for issues")
    public ResponseEntity<AvgResolutionTimeDTO> avgResolutionTime() {
        return ResponseEntity.ok(checklistIssueStatsUseCase.avgResolutionTime());
    }

    @GetMapping("/overdue-count")
    @Operation(summary = "Get overdue issues count", description = "Returns the count of overdue issues")
    public ResponseEntity<OverdueIssuesDTO> overdueCount() {
        return ResponseEntity.ok(checklistIssueStatsUseCase.overdueCount());
    }

    @GetMapping("/issues-per-execution")
    @Operation(summary = "Get issues per execution", description = "Returns the average number of issues per execution")
    public ResponseEntity<IssuesPerExecutionDTO> issuesPerExecution() {
        return ResponseEntity.ok(checklistIssueStatsUseCase.issuesPerExecution());
    }

    @GetMapping("/top-failing-items")
    @Operation(summary = "Get top failing items", description = "Returns the top failing items based on issue count")
    public ResponseEntity<List<StatsEntryDTO>> topFailingItems(
            @RequestParam @Parameter(description = "Maximum number of results to return", example = "10") Integer limit) {

        // Validate limit parameter
        validateLimit(limit);

        return ResponseEntity.ok(checklistIssueStatsUseCase.topFailingItems(limit));
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