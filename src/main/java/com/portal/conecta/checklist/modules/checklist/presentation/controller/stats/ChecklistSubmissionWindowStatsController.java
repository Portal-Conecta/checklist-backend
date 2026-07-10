package com.portal.conecta.checklist.modules.checklist.presentation.controller.stats;

import com.portal.conecta.checklist.modules.checklist.application.usecase.stats.ChecklistSubmissionWindowStatsUseCase;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.AvgFillTimeEntryDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.StatsEntryDTO;
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
@RequestMapping("/api/stats/submission-windows")
@RequiredArgsConstructor
@Tag(name = "Submission Window Statistics", description = "Endpoints for submission window statistics")
public class ChecklistSubmissionWindowStatsController {

    private final ChecklistSubmissionWindowStatsUseCase checklistSubmissionWindowStatsUseCase;

    @GetMapping("/count-by-type")
    @Operation(summary = "Get submission window count by type", description = "Returns the count of submission windows grouped by type")
    public ResponseEntity<List<StatsEntryDTO>> countByType() {
        return ResponseEntity.ok(checklistSubmissionWindowStatsUseCase.countByType());
    }

    @GetMapping("/count-by-shift")
    @Operation(summary = "Get submission window count by shift", description = "Returns the count of submission windows grouped by shift")
    public ResponseEntity<List<StatsEntryDTO>> countByShift() {
        return ResponseEntity.ok(checklistSubmissionWindowStatsUseCase.countByShift());
    }

    @GetMapping("/avg-duration-by-type")
    @Operation(summary = "Get average duration by type", description = "Returns the average duration grouped by type")
    public ResponseEntity<List<AvgFillTimeEntryDTO>> avgDurationByType() {
        return ResponseEntity.ok(checklistSubmissionWindowStatsUseCase.avgDurationByType());
    }
}