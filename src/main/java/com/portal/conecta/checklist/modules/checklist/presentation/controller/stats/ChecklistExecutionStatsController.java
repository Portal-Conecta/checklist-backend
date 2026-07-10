package com.portal.conecta.checklist.modules.checklist.presentation.controller.stats;

import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsRequestDTO;
import com.portal.conecta.checklist.modules.checklist.application.usecase.stats.ChecklistExecutionStatsUseCase;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.*;
import com.portal.conecta.checklist.shared.exception.InvalidRequestException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stats/executions")
@RequiredArgsConstructor
@Tag(name = "Execution Statistics", description = "Endpoints for execution statistics")
public class ChecklistExecutionStatsController {

    private final ChecklistExecutionStatsUseCase checklistExecutionStatsUseCase;

    @GetMapping("/count-by-day")
    @Operation(summary = "Get execution count by day", description = "Returns the count of executions grouped by day for the specified period")
    public ResponseEntity<List<StatsEntryDTO>> countByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String groupBy) {

        // Validate groupBy parameter
        if (groupBy != null && !groupBy.isEmpty()) {
            validateGroupBy(groupBy);
        }

        // Validate date range
        validateDateRange(from, to);

        return ResponseEntity.ok(checklistExecutionStatsUseCase.countByDay(from, to));
    }

    @GetMapping("/count-by-status")
    @Operation(summary = "Get execution count by status", description = "Returns the count of executions grouped by status")
    public ResponseEntity<List<StatsEntryDTO>> countByStatus() {
        return ResponseEntity.ok(checklistExecutionStatsUseCase.countByStatus());
    }

    @GetMapping("/count-by-type")
    @Operation(summary = "Get execution count by type", description = "Returns the count of executions grouped by type")
    public ResponseEntity<List<StatsEntryDTO>> countByType() {
        return ResponseEntity.ok(checklistExecutionStatsUseCase.countByType());
    }

    @GetMapping("/count-by-shift")
    @Operation(summary = "Get execution count by shift", description = "Returns the count of executions grouped by shift")
    public ResponseEntity<List<StatsEntryDTO>> countByShift() {
        return ResponseEntity.ok(checklistExecutionStatsUseCase.countByShift());
    }

    @GetMapping("/count-by-period")
    @Operation(summary = "Get execution count by period", description = "Returns the count of executions grouped by period")
    public ResponseEntity<List<StatsEntryDTO>> countByPeriod() {
        return ResponseEntity.ok(checklistExecutionStatsUseCase.countByPeriod());
    }

    @GetMapping("/completion-rate")
    @Operation(summary = "Get completion rate", description = "Returns the completion rate of executions")
    public ResponseEntity<CompletionRateDTO> completionRate() {
        return ResponseEntity.ok(checklistExecutionStatsUseCase.completionRate());
    }

    @GetMapping("/avg-fill-time-by-day")
    @Operation(summary = "Get average fill time by day", description = "Returns the average fill time grouped by day for the specified period")
    public ResponseEntity<List<AvgFillTimeEntryDTO>> avgFillTimeByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        // Validate date range
        validateDateRange(from, to);

        return ResponseEntity.ok(checklistExecutionStatsUseCase.avgFillTimeByDay(from, to));
    }

    @GetMapping("/count-by-day-and-status")
    @Operation(summary = "Get execution count by day and status", description = "Returns the count of executions grouped by day and status for the specified period")
    public ResponseEntity<List<StatsEntryDTO>> countByDayAndStatus(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        // Validate date range
        validateDateRange(from, to);

        return ResponseEntity.ok(checklistExecutionStatsUseCase.countByDayAndStatus(from, to));
    }

    @GetMapping("/with-issues-rate")
    @Operation(summary = "Get rate of executions with issues", description = "Returns the rate of executions that have issues")
    public ResponseEntity<WithIssuesRateDTO> withIssuesRate() {
        return ResponseEntity.ok(checklistExecutionStatsUseCase.withIssuesRate());
    }

    @GetMapping("/heatmap-shift-by-day-of-week")
    @Operation(summary = "Get heatmap of shifts by day of week", description = "Returns a heatmap showing shift distribution by day of week")
    public ResponseEntity<List<HeatmapEntryDTO>> heatmapShiftByDayOfWeek() {
        return ResponseEntity.ok(checklistExecutionStatsUseCase.heatmapShiftByDayOfWeek());
    }

    private void validateGroupBy(String groupBy) {
        // Valid groupBy values for execution stats
        var validValues = List.of("type", "shift");
        if (!validValues.contains(groupBy.toLowerCase())) {
            throw new InvalidRequestException(
                    "Invalid groupBy parameter. Valid values are: " + String.join(", ", validValues));
        }
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