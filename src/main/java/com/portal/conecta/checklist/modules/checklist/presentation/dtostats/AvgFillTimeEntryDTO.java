package com.portal.conecta.checklist.modules.checklist.presentation.dto.stats;

/**
 * {label (data), avgSeconds} — tempo médio de preenchimento por dia
 */
public record AvgFillTimeEntryDTO(String label, Double avgSeconds) {}