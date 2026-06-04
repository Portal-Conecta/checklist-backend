package com.portal.conecta.checklist.module.checklist.application.usecase.execution.command.core;

import com.portal.conecta.checklist.module.checklist.domain.enums.ConformityAnswerValue;
import com.portal.conecta.checklist.module.checklist.presentation.dto.request.ChecklistAnswerRequestDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ChecklistExecutionScoringService {


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
