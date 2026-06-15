package com.portal.conecta.checklist.module.checklist.presentation.dto.request;

import com.portal.conecta.checklist.module.checklist.domain.enums.ConformityAnswerValue;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

/**
 * DTO de entrada para a resposta de um item do checklist.
 *
 * <p>Representa o valor de conformidade enviado no submit e a observacao
 * opcional exigida em cenarios de nao conformidade.</p>
 */
public record ChecklistAnswerRequestDTO(
        @NotBlank(message = "itemKey e obrigatorio.")
        String itemKey,

        ConformityAnswerValue value,

        String observation,

        Instant answeredAt
) {}