package com.portal.conecta.checklist.module.checklist.presentation.dto;

import com.portal.conecta.checklist.module.checklist.presentation.dto.checklistExecution.ChecklistExecutionCreateDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ChecklistExecutionDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    @DisplayName("Não deve conter erros de validação se o DTO de Execução for válido")
    void executionDtoValidoNaoDeveTerErros() {
        ChecklistExecutionCreateDTO dto = new ChecklistExecutionCreateDTO(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Map.of("pergunta1", "resposta1")
        );

        Set<ConstraintViolation<ChecklistExecutionCreateDTO>> violacoes = validator.validate(dto);

        assertTrue(violacoes.isEmpty(), "O DTO válido de execução não deveria gerar erros de validação");
    }

    @Test
    @DisplayName("Deve capturar falhas de validação se os parâmetros obrigatórios forem nulos")
    void deveCapturarErrosDeIdsNulosExecution() {
        ChecklistExecutionCreateDTO dto = new ChecklistExecutionCreateDTO(
                null,
                null,
                null,
                null,
                null
        );

        Set<ConstraintViolation<ChecklistExecutionCreateDTO>> violacoes = validator.validate(dto);

        assertTrue(violacoes.size() >= 1, "O validador deveria encontrar erros para as propriedades nulas");
    }
}