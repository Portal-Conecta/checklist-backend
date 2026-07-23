package com.portal.conecta.checklist.module.issues.infrastructure.adapter.checklist;

import com.portal.conecta.checklist.module.checklist.application.port.out.issue.IssueStatsPort;
import com.portal.conecta.checklist.module.issues.application.usecase.query.ChecklistIssueStatsUseCase;
import com.portal.conecta.checklist.shared.stats.StatsEntryDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Adaptador do modulo Issues para {@link IssueStatsPort} (dono: modulo
 * Checklist) — ver ADR-0020. Repassa integralmente para
 * {@link ChecklistIssueStatsUseCase}, ja existente.
 */
@Component
@RequiredArgsConstructor
public class IssueStatsAdapter implements IssueStatsPort {

    private final ChecklistIssueStatsUseCase issueStatsUseCase;

    @Override
    public List<StatsEntryDTO> countByDay(LocalDate from, LocalDate to) {
        return issueStatsUseCase.countByDay(from, to);
    }

    @Override
    public List<StatsEntryDTO> countByStatus() {
        return issueStatsUseCase.countByStatus();
    }

    @Override
    public List<StatsEntryDTO> countByPriority() {
        return issueStatsUseCase.countByPriority();
    }
}
