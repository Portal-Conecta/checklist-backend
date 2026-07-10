package com.portal.conecta.checklist.modules.checklist.application.usecase.stats;

import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistTemplateStatsPort;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.*;
import com.portal.conecta.checklist.shared.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChecklistTemplateStatsUseCase {

    private final ChecklistTemplateStatsPort checklistTemplateStatsPort;

    public List<StatsEntryDTO> countByStatus() {
        return checklistTemplateStatsPort.countByStatus();
    }

    public List<StatsEntryDTO> countByActive() {
        return checklistTemplateStatsPort.countByActive();
    }

    public List<StatsEntryDTO> countByDay(LocalDate from, LocalDate to) {
        // Defense-in-depth validation
        validateDateRange(from, to);
        return checklistTemplateStatsPort.countByDay(from, to);
    }

    public List<StatsEntryDTO> countVersionsByGroup() {
        return checklistTemplateStatsPort.countVersionsByGroup();
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