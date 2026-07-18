package com.portal.conecta.checklist.modules.checklist.application.usecase.template.query;

import com.portal.conecta.checklist.modules.checklist.application.port.out.persistence.ChecklistTemplateStatsPort;
import com.portal.conecta.checklist.modules.checklist.application.dto.stats.StatsEntryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Caso de uso para agregação de métricas de templates de checklist.
 *
 * <p>Delega integralmente ao {@link ChecklistTemplateStatsPort} — toda a agregação
 * acontece no banco.</p>
 */
@Service
@RequiredArgsConstructor
public class ChecklistTemplateStatsUseCase {

    private final ChecklistTemplateStatsPort statsPort;

    /** Contagem de templates por status. */
    public List<StatsEntryDTO> countByStatus() {
        return statsPort.countByStatus();
    }

    /** Contagem de templates por flag {@code active}. */
    public List<StatsEntryDTO> countByActive() {
        return statsPort.countByActive();
    }

    /** Contagem de templates criados por dia. */
    public List<StatsEntryDTO> countByDay() {
        return statsPort.countByDay();
    }

    /** Número de versões por grupo de template. */
    public List<StatsEntryDTO> countVersionsByGroup() {
        return statsPort.countVersionsByGroup();
    }
}
