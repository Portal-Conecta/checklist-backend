package com.portal.conecta.checklist.module.issues.presentation.dto.request;


import com.portal.conecta.checklist.module.issues.presentation.dto.enums.IssuePriority;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateIssueRequestDTOTest {

    private Validator validator;

    private final LocalDateTime futureDate = LocalDateTime.now().plusDays(30);

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("should pass validation when all fields are valid")
    void shouldPassValidationWhenAllFieldsAreValid() {
        CreateIssueRequestDTO dto = new CreateIssueRequestDTO(
                UUID.randomUUID(),
                "item_007",
                "Uso de EPI obrigatório",
                UUID.randomUUID(),
                "EPI ausente no posto de trabalho",
                "Colaborador não utilizava capacete durante atividade de risco.",
                IssuePriority.HIGH,
                futureDate
        );

        Set<ConstraintViolation<CreateIssueRequestDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("should fail validation when executionId is null")
    void shouldFailValidationWhenExecutionIdIsNull() {
        CreateIssueRequestDTO dto = new CreateIssueRequestDTO(
                null,
                "item_007",
                "Uso de EPI obrigatório",
                UUID.randomUUID(),
                "EPI ausente no posto de trabalho",
                "Colaborador sem capacete.",
                IssuePriority.HIGH,
                futureDate
        );

        Set<ConstraintViolation<CreateIssueRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("executionId", violations.iterator().next().getPropertyPath().toString());
        assertEquals("O ID da execução é obrigatório.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation when itemKey is blank")
    void shouldFailValidationWhenItemKeyIsBlank() {
        CreateIssueRequestDTO dto = new CreateIssueRequestDTO(
                UUID.randomUUID(),
                "   ",
                "Uso de EPI obrigatório",
                UUID.randomUUID(),
                "EPI ausente no posto de trabalho",
                "Colaborador sem capacete.",
                IssuePriority.MEDIUM,
                futureDate
        );

        Set<ConstraintViolation<CreateIssueRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().allMatch(v -> v.getPropertyPath().toString().equals("itemKey")));
    }

    @Test
    @DisplayName("should fail validation when itemKey exceeds maximum length")
    void shouldFailValidationWhenItemKeyExceedsMaximumLength() {
        CreateIssueRequestDTO dto = new CreateIssueRequestDTO(
                UUID.randomUUID(),
                "A".repeat(256),
                "Uso de EPI obrigatório",
                UUID.randomUUID(),
                "EPI ausente no posto de trabalho",
                "Colaborador sem capacete.",
                IssuePriority.LOW,
                futureDate
        );

        Set<ConstraintViolation<CreateIssueRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("itemKey", violations.iterator().next().getPropertyPath().toString());
        assertEquals("A chave do item não pode ultrapassar 255 caracteres.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation when itemTitleSnapshot is blank")
    void shouldFailValidationWhenItemTitleSnapshotIsBlank() {
        CreateIssueRequestDTO dto = new CreateIssueRequestDTO(
                UUID.randomUUID(),
                "item_007",
                "   ",
                UUID.randomUUID(),
                "EPI ausente no posto de trabalho",
                "Colaborador sem capacete.",
                IssuePriority.LOW,
                futureDate
        );

        Set<ConstraintViolation<CreateIssueRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().allMatch(v -> v.getPropertyPath().toString().equals("itemTitleSnapshot")));
    }

    @Test
    @DisplayName("should fail validation when itemTitleSnapshot exceeds maximum length")
    void shouldFailValidationWhenItemTitleSnapshotExceedsMaximumLength() {
        CreateIssueRequestDTO dto = new CreateIssueRequestDTO(
                UUID.randomUUID(),
                "item_007",
                "A".repeat(256),
                UUID.randomUUID(),
                "EPI ausente no posto de trabalho",
                "Colaborador sem capacete.",
                IssuePriority.LOW,
                futureDate
        );

        Set<ConstraintViolation<CreateIssueRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("itemTitleSnapshot", violations.iterator().next().getPropertyPath().toString());
        assertEquals("O título snapshot não pode ultrapassar 255 caracteres.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation when assignedTo is null")
    void shouldFailValidationWhenAssignedToIsNull() {
        CreateIssueRequestDTO dto = new CreateIssueRequestDTO(
                UUID.randomUUID(),
                "item_007",
                "Uso de EPI obrigatório",
                null,
                "EPI ausente no posto de trabalho",
                "Colaborador sem capacete.",
                IssuePriority.HIGH,
                futureDate
        );

        Set<ConstraintViolation<CreateIssueRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("assignedTo", violations.iterator().next().getPropertyPath().toString());
        assertEquals("O responsável pela resolução é obrigatório.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation when title is blank")
    void shouldFailValidationWhenTitleIsBlank() {
        CreateIssueRequestDTO dto = new CreateIssueRequestDTO(
                UUID.randomUUID(),
                "item_007",
                "Uso de EPI obrigatório",
                UUID.randomUUID(),
                "   ",
                "Colaborador sem capacete.",
                IssuePriority.LOW,
                futureDate
        );

        Set<ConstraintViolation<CreateIssueRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().allMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    @DisplayName("should fail validation when title is too short")
    void shouldFailValidationWhenTitleIsTooShort() {
        CreateIssueRequestDTO dto = new CreateIssueRequestDTO(
                UUID.randomUUID(),
                "item_007",
                "Uso de EPI obrigatório",
                UUID.randomUUID(),
                "EPI",
                "Colaborador sem capacete.",
                IssuePriority.LOW,
                futureDate
        );

        Set<ConstraintViolation<CreateIssueRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("title", violations.iterator().next().getPropertyPath().toString());
        assertEquals("O título deve ter entre 5 e 150 caracteres.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation when title exceeds maximum length")
    void shouldFailValidationWhenTitleExceedsMaximumLength() {
        CreateIssueRequestDTO dto = new CreateIssueRequestDTO(
                UUID.randomUUID(),
                "item_007",
                "Uso de EPI obrigatório",
                UUID.randomUUID(),
                "A".repeat(151),
                "Colaborador sem capacete.",
                IssuePriority.LOW,
                futureDate
        );

        Set<ConstraintViolation<CreateIssueRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("title", violations.iterator().next().getPropertyPath().toString());
        assertEquals("O título deve ter entre 5 e 150 caracteres.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation when description is blank")
    void shouldFailValidationWhenDescriptionIsBlank() {
        CreateIssueRequestDTO dto = new CreateIssueRequestDTO(
                UUID.randomUUID(),
                "item_007",
                "Uso de EPI obrigatório",
                UUID.randomUUID(),
                "EPI ausente no posto",
                "   ",
                IssuePriority.LOW,
                futureDate
        );

        Set<ConstraintViolation<CreateIssueRequestDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().allMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    @DisplayName("should fail validation when description exceeds maximum length")
    void shouldFailValidationWhenDescriptionExceedsMaximumLength() {
        CreateIssueRequestDTO dto = new CreateIssueRequestDTO(
                UUID.randomUUID(),
                "item_007",
                "Uso de EPI obrigatório",
                UUID.randomUUID(),
                "EPI ausente no posto",
                "A".repeat(2001),
                IssuePriority.LOW,
                futureDate
        );

        Set<ConstraintViolation<CreateIssueRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("description", violations.iterator().next().getPropertyPath().toString());
        assertEquals("A descrição não pode ultrapassar 2000 caracteres.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation when priority is null")
    void shouldFailValidationWhenPriorityIsNull() {
        CreateIssueRequestDTO dto = new CreateIssueRequestDTO(
                UUID.randomUUID(),
                "item_007",
                "Uso de EPI obrigatório",
                UUID.randomUUID(),
                "EPI ausente no posto",
                "Colaborador sem capacete.",
                null,
                futureDate
        );

        Set<ConstraintViolation<CreateIssueRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("priority", violations.iterator().next().getPropertyPath().toString());
        assertEquals("A prioridade é obrigatória.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation when dueAt is null")
    void shouldFailValidationWhenDueAtIsNull() {
        CreateIssueRequestDTO dto = new CreateIssueRequestDTO(
                UUID.randomUUID(),
                "item_007",
                "Uso de EPI obrigatório",
                UUID.randomUUID(),
                "EPI ausente no posto",
                "Colaborador sem capacete.",
                IssuePriority.HIGH,
                null
        );

        Set<ConstraintViolation<CreateIssueRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("dueAt", violations.iterator().next().getPropertyPath().toString());
        assertEquals("O prazo de resolução é obrigatório.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation when dueAt is in the past")
    void shouldFailValidationWhenDueAtIsInThePast() {
        CreateIssueRequestDTO dto = new CreateIssueRequestDTO(
                UUID.randomUUID(),
                "item_007",
                "Uso de EPI obrigatório",
                UUID.randomUUID(),
                "EPI ausente no posto",
                "Colaborador sem capacete.",
                IssuePriority.HIGH,
                LocalDateTime.now().minusDays(1)
        );

        Set<ConstraintViolation<CreateIssueRequestDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("dueAt", violations.iterator().next().getPropertyPath().toString());
        assertEquals("O prazo deve ser uma data futura.", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("should fail validation when all required fields are null")
    void shouldFailValidationWhenAllRequiredFieldsAreNull() {
        CreateIssueRequestDTO dto = new CreateIssueRequestDTO(
                null, null, null, null, null, null, null, null
        );

        Set<ConstraintViolation<CreateIssueRequestDTO>> violations = validator.validate(dto);

        assertEquals(8, violations.size());
    }
}