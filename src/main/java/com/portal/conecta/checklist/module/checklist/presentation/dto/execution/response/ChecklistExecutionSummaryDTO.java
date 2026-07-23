package com.portal.conecta.checklist.module.checklist.presentation.dto.execution.response;

/**
 * DTO de resumo das respostas de uma execucao.
 *
 * <p>Apresenta totais agregados de itens respondidos, conformes e nao
 * conformes para consumo em telas e dashboards.</p>
 */
public record ChecklistExecutionSummaryDTO(
        Integer totalItems,
        Integer answeredItems,
        Integer compliantItems,
        Integer nonCompliantItems
) {}
