package com.portal.conecta.checklist.modules.checklist.presentation.dto.stats;

/**
 * {shift, dayOfWeek, count} — célula do heatmap turno × dia da semana
 */
public record HeatmapEntryDTO(String shift, Integer dayOfWeek, Long count) {}