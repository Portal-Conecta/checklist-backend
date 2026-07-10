package com.portal.conecta.checklist.modules.checklist.application.port.out.persistence;

import com.portal.conecta.checklist.modules.checklist.presentation.dto.stats.StatsEntryDTO;

import java.util.List;

/**
 * Port for retrieving statistics about checklist templates.
 */
public interface ChecklistTemplateStatsPort {

    List<StatsEntryDTO> countByStatus();

    List<StatsEntryDTO> countByActive();

    List<StatsEntryDTO> countByDay();

    List<StatsEntryDTO> countVersionsByGroup();
}