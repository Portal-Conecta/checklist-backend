package com.portal.conecta.checklist.module.checklist.presentation.dto;

import com.portal.conecta.checklist.module.checklist.presentation.dto.checklistTemplate.ChecklistTemplateCreateDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ChecklistTemplateDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    @DisplayName("Não deve conter erros de validação se o DTO de Template for válido")
    void templateDtoValidoNaoDeveTerErros() {
        ChecklistTemplateCreateDTO dto = new ChecklistTemplateCreateDTO(
                UUID.randomUUID(),
                "Título Válido",
                "Descrição Válida",
                mock(UserToken.class)
        );

        Set<ConstraintViolation<ChecklistTemplateCreateDTO>> violacoes = validator.validate(dto);

        assertTrue(violacoes.isEmpty(), "O DTO válido não deveria disparar erros de validação");
    }

    @Test
    @DisplayName("Deve capturar falhas de validação se os campos forem inválidos ou vazios")
    void deveCapturarErrosCamposObrigatoriosTemplate() {
        ChecklistTemplateCreateDTO dto = new ChecklistTemplateCreateDTO(
                null,
                "",
                "   ",
                null
        );

        Set<ConstraintViolation<ChecklistTemplateCreateDTO>> violacoes = validator.validate(dto);

        assertTrue(violacoes.size() >= 1, "O validador deveria encontrar erros para os campos inválidos");
    }
}