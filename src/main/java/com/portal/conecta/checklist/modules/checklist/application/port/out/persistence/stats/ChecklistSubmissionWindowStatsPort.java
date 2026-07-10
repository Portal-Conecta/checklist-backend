package com.portal.conecta.checklist.modules.checklist.application.port.out.persistence;

import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.AvgFillTimeEntryDTO;
import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.StatsEntryDTO;

import java.util.List;

/**
 * Port for retrieving statistics about submission windows.
 */
public interface ChecklistSubmissionWindowStatsPort {

    List<StatsEntryDTO> countByType();

    List<StatsEntryDTO> countByShift();

    List<AvgFillTimeEntryDTO> avgDurationByType();
}