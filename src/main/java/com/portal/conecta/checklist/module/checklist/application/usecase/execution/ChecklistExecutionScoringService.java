package com.portal.conecta.checklist.module.checklist.application.usecase.execution;

import com.portal.conecta.checklist.module.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistAnswerRequestDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Serviço responsável pelo cálculo da nota de conformidade (Compliance Score),
 * compartilhado entre os fluxos de submissão e edição de execuções de checklist.
 */
@Service
public class ChecklistExecutionScoringService {

    /**
     * Calcula a nota de conformidade (Compliance Score) com base nas respostas avaliadas.
     * O cálculo é feito dividindo o número de itens conformes pelo total de itens respondidos.
     *
     * @param answers a lista de respostas fornecidas pelo usuário.
     * @return o percentual de conformidade de 0.00 a 100.00, com duas casas decimais.
     */
    public BigDecimal calculateComplianceScore(List<ChecklistAnswerRequestDTO> answers) {
        long answeredItems = answers.stream()
                .filter(answer -> answer.value() != null)
                .count();

        if (answeredItems == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        long compliantItems = answers.stream()
                .filter(answer -> answer.value() == ConformityAnswerValue.COMPLIANT)
                .count();

        return BigDecimal.valueOf(compliantItems)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(answeredItems), 2, RoundingMode.HALF_UP);
    }
}
