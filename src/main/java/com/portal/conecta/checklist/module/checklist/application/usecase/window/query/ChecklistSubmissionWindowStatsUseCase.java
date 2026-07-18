package com.portal.conecta.checklist.module.checklist.application.usecase.window.query;

import com.portal.conecta.checklist.module.checklist.application.port.out.persistence.ChecklistSubmissionWindowStatsPort;
import com.portal.conecta.checklist.shared.stats.StatsEntryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso de uso para agregação de métricas de janelas de submissão.
 *
 * <p>Delega integralmente ao {@link ChecklistSubmissionWindowStatsPort} — toda a
 * agregação acontece no banco.</p>
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

    /** Duração média em minutos por tipo de checklist. */
    public List<StatsEntryDTO> avgDurationByType() {
        return statsPort.avgDurationByType();
    }
}
