package com.portal.conecta.checklist.module.checklist.presentation.dto.request;
 
import jakarta.validation.Valid;
import java.util.List;
 
/**
 * DTO de entrada para o salvamento parcial (rascunho) de uma execução de checklist no Redis.
 *
 * <p>Diferente do {@link ChecklistExecutionSubmitDTO}, a lista pode estar vazia ou
 * incompleta — o rascunho não exige que todos os itens obrigatórios sejam respondidos.</p>
 *
 * <p>Itens ausentes no payload são preservados da versão anterior já armazenada no Redis.
 * Itens presentes sobrescrevem o valor anterior para aquela {@code itemKey}.</p>
 */
public record ChecklistExecutionSaveDraftDTO(
        @Valid
        List<ChecklistAnswerRequestDTO> answers
) {
    public ChecklistExecutionSaveDraftDTO {
        answers = answers == null ? List.of() : List.copyOf(answers);
    }
}