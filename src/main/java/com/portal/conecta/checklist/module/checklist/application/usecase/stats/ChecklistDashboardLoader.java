package com.portal.conecta.checklist.module.checklist.application.usecase.stats;

import com.portal.conecta.checklist.module.checklist.application.dto.stats.DashboardStatsResponseDTO;
import com.portal.conecta.checklist.module.checklist.application.port.out.issue.IssueStatsPort;
import com.portal.conecta.checklist.module.checklist.application.usecase.execution.query.ChecklistExecutionStatsUseCase;
import com.portal.conecta.checklist.shared.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Carrega e cacheia o payload do dashboard. A autorizacao NAO mora aqui:
 * fica no {@link ChecklistDashboardUseCase}, que roda antes de qualquer
 * lookup no cache — senao um hit de cache (chave so de datas) pularia o
 * {@code canViewDashboard()} e vaza dados para perfis sem permissao.
 */
@Service
@RequiredArgsConstructor
public class ChecklistDashboardLoader {

    private final ChecklistExecutionStatsUseCase executionStats;
    private final IssueStatsPort issueStats;

    @Cacheable(cacheNames = CacheConfig.DASHBOARD_CACHE, key = "#from + '_' + #to")
    public DashboardStatsResponseDTO load(LocalDate from, LocalDate to) {
        return new DashboardStatsResponseDTO(
                new DashboardStatsResponseDTO.Periodo(from, to),
                executionStats.countByDay(from, to),
                executionStats.countByStatus(),
                executionStats.completionRate(),
                issueStats.countByStatus(),
                issueStats.countByPriority(),
                issueStats.countByDay(from, to)
        );
    }
}
