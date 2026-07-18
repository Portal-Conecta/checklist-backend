package com.portal.conecta.checklist.module.checklist.application.port.out.issue;

import com.portal.conecta.checklist.shared.stats.StatsEntryDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Porta de saida do modulo Checklist para o modulo Issues: agregados de
 * pendencias consumidos pelo dashboard composto (ver ADR-0020).
 *
 * <p>Implementada por um adaptador dentro do modulo Issues, que delega para o
 * {@code ChecklistIssueStatsUseCase} ja existente.</p>
 */
public interface IssueStatsPort {

    List<StatsEntryDTO> countByDay(LocalDate from, LocalDate to);

    List<StatsEntryDTO> countByStatus();

    List<StatsEntryDTO> countByPriority();
}
