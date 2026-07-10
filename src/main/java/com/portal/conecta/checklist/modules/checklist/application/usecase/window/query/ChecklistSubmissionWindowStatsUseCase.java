package com.portal.conecta.checklist.modules.checklist.application.usecase.window.query;

import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistSubmissionWindowStatsPort;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.AvgFillTimeEntryDTO;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso de uso para agregaÃ§Ã£o de mÃ©tricas de janelas de submissÃ£o.
 *
 * <p>Delega integralmente ao {@link ChecklistSubmissionWindowStatsPort} â€” toda a
 * agregaÃ§Ã£o acontece no banco.</p>
 */
@Service
@RequiredArgsConstructor
public class ChecklistSubmissionWindowStatsUseCase {

    private final ChecklistSubmissionWindowStatsPort statsPort;

    /** Contagem de janelas por tipo de checklist. */
    public List<StatsEntryDTO> countByType() {
        return statsPort.countByType();
    }

    /** Contagem de janelas por turno. */
    public List<StatsEntryDTO> countByShift() {
        return statsPort.countByShift();
    }

    /** DuraÃ§Ã£o mÃ©dia em minutos por tipo de checklist. */
    public List<AvgFillTimeEntryDTO> avgDurationByType() {
        return statsPort.avgDurationByType();
    }
}
