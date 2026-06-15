package com.portal.conecta.checklist.modules.checklist.presentation.dto.execution.response;

import com.portal.conecta.checklist.modules.checklist.domain.enums.ConformityAnswerValue;

import java.time.Instant;

/**
 * DTO de resposta para uma resposta registrada em checklist.
 *
 * <p>Exp?e o item respondido, o valor de conformidade, a observacao e o
 * instante em que a resposta foi informada.</p>
 */
public record ChecklistAnswerResponseDTO(
        String itemKey,
        ConformityAnswerValue value,
        Boolean compliant,
        String observation,
        Instant answeredAt
) {}
