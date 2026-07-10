package com.portal.conecta.checklist.modules.checklist.presentation.controller.stats;

import com.portal.conecta.checklist.modules.checklist.application.usecase.stats.ChecklistTemplateStatsUseCase;
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
@RequestMapping("/api/stats/templates")
@RequiredArgsConstructor
@Tag(name = "Template Statistics", description = "Endpoints for template statistics")
public class ChecklistTemplateStatsController {

    private final ChecklistTemplateStatsUseCase checklistTemplateStatsUseCase;

    @GetMapping("/count-by-status")
    @Operation(summary = "Get template count by status", description = "Returns the count of templates grouped by status")
    public ResponseEntity<List<StatsEntryDTO>> countByStatus() {
        return ResponseEntity.ok(checklistTemplateStatsUseCase.countByStatus());
    }

    @GetMapping("/count-by-active")
    @Operation(summary = "Get template count by active status", description = "Returns the count of templates grouped by active status")
    public ResponseEntity<List<StatsEntryDTO>> countByActive() {
        return ResponseEntity.ok(checklistTemplateStatsUseCase.countByActive());
    }

    @GetMapping("/count-by-day")
    @Operation(summary = "Get template count by day", description = "Returns the count of templates grouped by day for the specified period")
    public ResponseEntity<List<StatsEntryDTO>> countByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        // Validate date range
        validateDateRange(from, to);

        return ResponseEntity.ok(checklistTemplateStatsUseCase.countByDay(from, to));
    }

    @GetMapping("/count-versions-by-group")
    @Operation(summary = "Get template versions count by group", description = "Returns the count of template versions grouped by group")
    public ResponseEntity<List<StatsEntryDTO>> countVersionsByGroup() {
        return ResponseEntity.ok(checklistTemplateStatsUseCase.countVersionsByGroup());
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