package com.portal.conecta.checklist.modules.checklist.application.usecase.stats;

import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistSubmissionWindowStatsPort;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.AvgFillTimeEntryDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.StatsEntryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChecklistSubmissionWindowStatsUseCase {

    private final ChecklistSubmissionWindowStatsPort checklistSubmissionWindowStatsPort;

    public List<StatsEntryDTO> countByType() {
        return checklistSubmissionWindowStatsPort.countByType();
    }

    public List<StatsEntryDTO> countByShift() {
        return checklistSubmissionWindowStatsPort.countByShift();
    }

    public List<AvgFillTimeEntryDTO> avgDurationByType() {
        return checklistSubmissionWindowStatsPort.avgDurationByType();
    }
}