package com.portal.conecta.checklist.modules.checklist.presentation.dto.stats;

/**
 * Formato genérico {label, value} — serve pra qualquer gráfico de barra/pizza/linha
 */
public record StatsEntryDTO(String label, Long value) {}