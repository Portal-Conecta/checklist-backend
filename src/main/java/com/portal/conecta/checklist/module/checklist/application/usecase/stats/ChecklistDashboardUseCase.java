package com.portal.conecta.checklist.module.checklist.application.usecase.stats;

import com.portal.conecta.checklist.module.checklist.application.dto.stats.DashboardStatsResponseDTO;
import com.portal.conecta.checklist.shared.context.RequestContextProvider;
import com.portal.conecta.checklist.shared.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Caso de uso composto do dashboard. Orquestra autorizacao, validacao de
 * intervalo e a carga (cacheada) dos graficos fixos.
 *
 * <p>Autorizacao roda SEMPRE antes do cache — ver {@link ChecklistDashboardLoader}.
 * Perfis permitidos: SENAI, WEG e ADMIN ({@code RequestContext#canViewDashboard}).</p>
 */
@Service
@RequiredArgsConstructor
public class ChecklistDashboardUseCase {

    /** Janela padrao das series temporais quando o cliente nao informa datas. */
    private static final int DEFAULT_RANGE_DAYS = 30;

    private final ChecklistDashboardLoader loader;
    private final RequestContextProvider contextProvider;

    /**
     * Monta o dashboard para o intervalo informado. Datas ausentes assumem os
     * ultimos {@value #DEFAULT_RANGE_DAYS} dias.
     */
    public DashboardStatsResponseDTO execute(LocalDate from, LocalDate to) {
        if (!contextProvider.getRequestContext().canViewDashboard()) {
            throw new AccessDeniedException(
                    "Apenas a gestao (SENAI/WEG/ADMIN) pode acessar o dashboard.");
        }

        LocalDate resolvedTo = (to == null) ? LocalDate.now() : to;
        LocalDate resolvedFrom = (from == null) ? resolvedTo.minusDays(DEFAULT_RANGE_DAYS) : from;
        validateRange(resolvedFrom, resolvedTo);

        return loader.load(resolvedFrom, resolvedTo);
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
