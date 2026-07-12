package com.portal.conecta.checklist.modules.checklist.application.usecase.stats;

import com.portal.conecta.checklist.modules.checklist.application.dto.stats.DashboardStatsResponseDTO;
import com.portal.conecta.checklist.modules.checklist.application.usecase.execution.query.ChecklistExecutionStatsUseCase;
import com.portal.conecta.checklist.modules.checklist.issues.application.usecase.query.ChecklistIssueStatsUseCase;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Caso de uso composto do dashboard. NAO executa nenhuma query propria: apenas
 * orquestra os use cases de agregacao granulares (execucoes e issues) e monta a
 * resposta unica do painel.
 *
 * <p>O resultado e cacheado (ver {@link Cacheable}) por uma janela curta, de modo
 * que multiplos acessos/pollings dentro dessa janela custem uma unica rodada de
 * agregacoes no banco, e nao uma por requisicao.</p>
 */
@Service
@RequiredArgsConstructor
public class ChecklistDashboardUseCase {

    /** Janela padrao das series temporais quando o cliente nao informa datas. */
    private static final int DEFAULT_RANGE_DAYS = 30;

    private final ChecklistExecutionStatsUseCase executionStats;
    private final ChecklistIssueStatsUseCase issueStats;
    private final RequestContextProvider contextProvider;

    /**
     * Monta o dashboard para o intervalo informado. Datas ausentes assumem os
     * ultimos {@value #DEFAULT_RANGE_DAYS} dias.
     *
     * <p>Cache: chave = {@code from + '_' + to} (valores crus do cliente). Como o
     * TTL e curto (configurado no CacheManager), a janela de "ultimos 30 dias"
     * com datas nulas fica no maximo defasada pelo TTL — trade-off aceitavel.
     * Excecoes nao sao cacheadas (comportamento padrao do Spring Cache).</p>
     */
    @Cacheable(cacheNames = "checklist-dashboard", key = "#from + '_' + #to")
    public DashboardStatsResponseDTO execute(LocalDate from, LocalDate to) {
        // Autorizacao: alinhada ao restante do modulo. Se o painel dever ser
        // restrito a gestao (SENAI/WEG), trocar canAccessChecklistModule() pelo
        // predicado correspondente (ex.: canManageIssues()).
        if (!contextProvider.getRequestContext().canAccessChecklistModule()) {
            throw new AccessDeniedException("Usuario nao tem permissao para acessar o modulo Checklist.");
        }

        LocalDate resolvedTo = (to == null) ? LocalDate.now() : to;
        LocalDate resolvedFrom = (from == null) ? resolvedTo.minusDays(DEFAULT_RANGE_DAYS) : from;
        validateRange(resolvedFrom, resolvedTo);

        return new DashboardStatsResponseDTO(
                new DashboardStatsResponseDTO.Periodo(resolvedFrom, resolvedTo),
                executionStats.countByDay(resolvedFrom, resolvedTo),
                executionStats.countByStatus(),
                executionStats.completionRate(),
                issueStats.countByStatus(),
                issueStats.countByPriority(),
                issueStats.countByDay(resolvedFrom, resolvedTo)
        );
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new InvalidRequestException("O parametro 'from' nao pode ser posterior a 'to'.");
        }
        if (to.isAfter(LocalDate.now())) {
            throw new InvalidRequestException("O intervalo nao pode estar no futuro.");
        }
    }
}
